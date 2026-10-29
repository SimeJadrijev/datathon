package com.datathon.api.ingest.extract;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.AmountParser.Amounts;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleExtractor {

    private static final int HEAD_CHARS = 400;
    private static final int SHORT_PAGE_CHARS = 500;
    private static final int NKD_WINDOW_CHARS = 300;
    private static final Pattern NKD_KEYWORD = Pattern.compile("\\bNKD\\b");
    private static final Pattern CANCELLED = Pattern.compile(
            "poništen|obustavljen|stavljen izvan snage", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern NKD_CODE = Pattern.compile("\\b(\\d{2}\\.\\d{1,2})\\b");

    private static final List<Map.Entry<String, FundingCallType>> TYPE_KEYWORDS = List.of(
            Map.entry("stipendij", FundingCallType.SCHOLARSHIP),
            Map.entry("zajam", FundingCallType.LOAN),
            Map.entry("zajmov", FundingCallType.LOAN),
            Map.entry("kredit", FundingCallType.LOAN),
            Map.entry("jamstv", FundingCallType.LOAN),
            Map.entry("nagrad", FundingCallType.AWARD),
            Map.entry("potpor", FundingCallType.SUBSIDY),
            Map.entry("poticaj", FundingCallType.SUBSIDY),
            Map.entry("sufinanc", FundingCallType.SUBSIDY),
            Map.entry("subvencij", FundingCallType.SUBSIDY),
            Map.entry("vaučer", FundingCallType.SUBSIDY),
            Map.entry("javni poziv", FundingCallType.PUBLIC_CALL),
            Map.entry("poziv", FundingCallType.PUBLIC_CALL),
            Map.entry("natječaj", FundingCallType.GRANT),
            Map.entry("natjecaj", FundingCallType.GRANT),
            Map.entry("mjer", FundingCallType.SUBSIDY));

    private final Clock clock;

    @Autowired
    public RuleExtractor() {
        this(Clock.systemDefaultZone());
    }

    RuleExtractor(Clock clock) {
        this.clock = clock;
    }

    public ExtractedFields extract(RawItem item) {
        String page = item.title() + "\n" + item.text();
        LocalDate deadline = deadline(item, page, item.publishedAt());
        LocalDate publishedAt = item.publishedAt() != null ? item.publishedAt() : guessPublishedAt(item, deadline);
        Amounts amounts = amounts(item, page);
        String body = item.text().length() > SHORT_PAGE_CHARS ? item.text() : item.text() + "\n" + item.pdfText();
        return new ExtractedFields(
                type(item),
                status(item, deadline),
                publishedAt,
                deadline,
                deadlineNote(item, deadline, page),
                amounts.total(),
                amounts.min(),
                amounts.max(),
                targetGroups(item, body),
                areas(item, body),
                activityCodes(page + "\n" + item.pdfText()),
                item.countiesHint());
    }

    private LocalDate deadline(RawItem item, String page, LocalDate publishedAt) {
        if (item.deadlineHint() != null) {
            return item.deadlineHint();
        }
        if (item.deadlineNoteHint() != null) {
            return null;
        }
        return DeadlineFinder.find(page, publishedAt)
                .or(() -> DeadlineFinder.find(item.pdfText(), publishedAt))
                .orElse(null);
    }

    private static String deadlineNote(RawItem item, LocalDate deadline, String page) {
        if (deadline != null) {
            return null;
        }
        if (item.deadlineNoteHint() != null) {
            return item.deadlineNoteHint();
        }
        return DeadlineFinder.findNote(page + "\n" + item.pdfText()).orElse(null);
    }

    private LocalDate guessPublishedAt(RawItem item, LocalDate deadline) {
        return CroatianDates.findAll(head(item.text())).stream()
                .map(CroatianDates.DateMatch::date)
                .filter(date -> deadline == null || date.isBefore(deadline))
                .findFirst()
                .orElse(null);
    }

    private Amounts amounts(RawItem item, String page) {
        Amounts found = AmountParser.pick(List.of(page, item.pdfText()));
        return new Amounts(
                preferHint(item.totalAmountHint(), found.total()),
                preferHint(item.minAmountHint(), found.min()),
                preferHint(item.maxAmountHint(), found.max()));
    }

    private FundingCallType type(RawItem item) {
        if (item.typeHint() != null) {
            return item.typeHint();
        }
        String title = item.title().toLowerCase(Locale.ROOT);
        return TYPE_KEYWORDS.stream()
                .filter(entry -> title.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private FundingCallStatus status(RawItem item, LocalDate deadline) {
        if (CANCELLED.matcher(item.title()).find()) {
            return FundingCallStatus.CLOSED;
        }
        if (item.statusHint() != null) {
            return item.statusHint();
        }
        String title = item.title().toLowerCase(Locale.ROOT);
        String head = (title + " " + head(item.text())).toLowerCase(Locale.ROOT);
        if (title.contains("najav")) {
            return FundingCallStatus.ANNOUNCED;
        }
        if (head.contains("zatvoreno") || head.contains("zatvoren je")) {
            return FundingCallStatus.CLOSED;
        }
        if (deadline == null) {
            return null;
        }
        return deadline.isBefore(LocalDate.now(clock)) ? FundingCallStatus.CLOSED : FundingCallStatus.OPEN;
    }

    private Set<TargetGroup> targetGroups(RawItem item, String body) {
        Set<TargetGroup> groups = EnumSet.noneOf(TargetGroup.class);
        groups.addAll(item.targetGroupsHint());
        groups.addAll(KeywordClassifier.targetGroups(item.title(), body));
        return groups;
    }

    private Set<Area> areas(RawItem item, String body) {
        String titleAndCategories = item.title() + " " + String.join(" ", item.categories());
        return KeywordClassifier.areas(titleAndCategories, body);
    }

    private Set<String> activityCodes(String text) {
        Set<String> codes = new TreeSet<>();
        Matcher keyword = NKD_KEYWORD.matcher(text);
        while (keyword.find()) {
            String window = text.substring(keyword.start(), Math.min(text.length(), keyword.start() + NKD_WINDOW_CHARS));
            Matcher code = NKD_CODE.matcher(window);
            while (code.find()) {
                codes.add(code.group(1));
            }
        }
        return codes;
    }

    private static BigDecimal preferHint(BigDecimal hint, BigDecimal found) {
        return hint != null ? hint : found;
    }

    private static String head(String text) {
        return text.substring(0, Math.min(text.length(), HEAD_CHARS));
    }
}
