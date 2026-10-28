package com.datathon.api.eligibility;

import com.datathon.api.integration.llm.LlmClient;
import com.datathon.api.integration.llm.LlmProperties;
import com.datathon.api.integration.llm.LlmRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * One model call that judges the answers. The model must quote the call; a quote that is not in the text is
 * dropped, and a verdict with no quote left becomes UNCLEAR, so a made-up reason never reaches the user.
 */
@Component
@RequiredArgsConstructor
public class EligibilityJudge {

    private static final int MAX_TEXT_CHARS = 40_000;
    private static final int MAX_REASONS = 5;
    private static final int MIN_QUOTE_LETTERS = 15;
    private static final String NL = System.lineSeparator();

    private final LlmClient llmClient;
    private final LlmProperties properties;

    /** Throws the LLM exceptions of {@link LlmClient}; the caller decides on the fallback. */
    public EligibilityCheckResponse judge(EligibilityContext context, Map<String, Object> answers) {
        JsonNode answer = llmClient.complete(new LlmRequest(
                EligibilityJudgeSchema.PURPOSE,
                properties.getEligibilityModel(),
                EligibilityJudgeSchema.SYSTEM_PROMPT,
                userText(context, answers),
                EligibilityJudgeSchema.SCHEMA));
        List<EligibilityReason> reasons = quotedReasons(answer, context.sourceText());
        List<String> missingInfo = strings(answer.path("missingInfo"));
        EligibilityVerdict verdict = parseVerdict(answer.path("verdict").asString(""));
        if (verdict != EligibilityVerdict.UNCLEAR && reasons.isEmpty()) {
            verdict = EligibilityVerdict.UNCLEAR;
            missingInfo.add("Procjenu nije moguće potkrijepiti citatom iz teksta poziva.");
        }
        return new EligibilityCheckResponse(verdict, reasons, missingInfo, EligibilityCheckResponse.DISCLAIMER,
                context.url());
    }

    private List<EligibilityReason> quotedReasons(JsonNode answer, String sourceText) {
        String squashedSource = TextMatch.squash(sourceText);
        List<EligibilityReason> reasons = new ArrayList<>();
        for (JsonNode item : answer.path("reasons")) {
            String text = item.path("text").asString("").trim();
            String quote = item.path("quote").asString("").trim();
            String squashedQuote = TextMatch.squash(quote);
            boolean isQuoted = squashedQuote.length() >= MIN_QUOTE_LETTERS && squashedSource.contains(squashedQuote);
            if (!text.isEmpty() && isQuoted && reasons.size() < MAX_REASONS) {
                reasons.add(new EligibilityReason(text, quote));
            }
        }
        return reasons;
    }

    private List<String> strings(JsonNode array) {
        List<String> values = new ArrayList<>();
        for (JsonNode element : array) {
            String value = element.asString("").trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private EligibilityVerdict parseVerdict(String name) {
        try {
            return EligibilityVerdict.valueOf(name);
        } catch (IllegalArgumentException e) {
            return EligibilityVerdict.UNCLEAR;
        }
    }

    private String userText(EligibilityContext context, Map<String, Object> answers) {
        StringBuilder text = new StringBuilder("Title: ").append(context.title()).append(NL).append(NL)
                .append("--- QUESTIONS AND APPLICANT ANSWERS ---").append(NL);
        for (EligibilityQuestion question : context.questions()) {
            text.append(question.id()).append(" (").append(question.type()).append("): ").append(question.text())
                    .append(" -> ").append(formatAnswer(answers.get(question.id()))).append(NL);
        }
        String source = context.sourceText();
        return text.append(NL).append("--- CALL TEXT ---").append(NL)
                .append(source, 0, Math.min(source.length(), MAX_TEXT_CHARS)).toString();
    }

    private String formatAnswer(Object answer) {
        if (answer == null) {
            return "(no answer)";
        }
        if (answer instanceof Boolean yes) {
            return yes ? "YES" : "NO";
        }
        return String.valueOf(answer);
    }
}
