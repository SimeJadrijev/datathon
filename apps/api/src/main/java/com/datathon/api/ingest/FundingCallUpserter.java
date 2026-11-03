package com.datathon.api.ingest;

import com.datathon.api.fundingcall.Attachment;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.LegalReference;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.source.SourceCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FundingCallUpserter {

    public enum Result { CREATED, UPDATED, UNCHANGED }

    static final int MAX_SOURCE_TEXT_CHARS = 80_000;

    private final FundingCallRepository fundingCallRepository;

    @Transactional
    public Result upsert(SourceCode source, RawItem item, ExtractedFields fields) {
        String sourceText = sourceText(item);
        String hash = sha256(item.title() + "\n" + sourceText);
        Optional<FundingCall> existing = fundingCallRepository.findBySourceAndExternalId(source, item.externalId());
        if (existing.isEmpty()) {
            FundingCall fundingCall = new FundingCall(source, item.externalId(), item.title());
            applyContent(fundingCall, item, fields, sourceText, hash);
            fundingCallRepository.save(fundingCall);
            return Result.CREATED;
        }
        FundingCall fundingCall = existing.get();
        fundingCall.setFetchedAt(Instant.now());
        if (fields.status() != null) {
            fundingCall.setStatus(fields.status());
        }
        if (hash.equals(fundingCall.getRawTextHash())) {
            return Result.UNCHANGED;
        }
        applyContent(fundingCall, item, fields, sourceText, hash);
        return Result.UPDATED;
    }

    private void applyContent(FundingCall fundingCall, RawItem item, ExtractedFields fields, String sourceText,
            String hash) {
        fundingCall.setUrl(item.url());
        fundingCall.setTitle(item.title());
        fundingCall.setProvider(item.provider());
        fundingCall.setType(fields.type());
        fundingCall.setStatus(fields.status());
        fundingCall.setPublishedAt(fields.publishedAt());
        replace(fundingCall.getCounties(), fields.counties());
        replace(fundingCall.getActivityCodes(), fields.activityCodes());
        replace(fundingCall.getAttachments(), item.attachments().stream()
                .map(attachment -> new Attachment(attachment.name(), attachment.url()))
                .toList());
        if (!fundingCall.isFieldsReviewed()) {
            applyExtractedValues(fundingCall, fields);
        }
        resetAiResults(fundingCall);
        replace(fundingCall.getLegalReferences(), LegalReference.fromText(sourceText));
        fundingCall.setSourceText(sourceText);
        fundingCall.setRawTextHash(hash);
        fundingCall.setFetchedAt(Instant.now());
    }

    private void applyExtractedValues(FundingCall fundingCall, ExtractedFields fields) {
        fundingCall.setDeadline(fields.deadline());
        fundingCall.setDeadlineNote(fields.deadlineNote());
        fundingCall.setTotalAmount(fields.totalAmount());
        fundingCall.setMinAmount(fields.minAmount());
        fundingCall.setMaxAmount(fields.maxAmount());
        replace(fundingCall.getTargetGroups(), fields.targetGroups());
        replace(fundingCall.getAreas(), fields.areas());
    }

    private void resetAiResults(FundingCall fundingCall) {
        fundingCall.setExtractedByLlm(false);
        fundingCall.getAiFields().clear();
        fundingCall.setConditionsSummary(null);
        fundingCall.getRequiredDocuments().clear();
        fundingCall.setEligibilityQuestions(null);
    }

    private static String sourceText(RawItem item) {
        String text = item.pdfText().isBlank() ? item.text() : item.text() + "\n\n" + item.pdfText();
        return text.length() > MAX_SOURCE_TEXT_CHARS ? text.substring(0, MAX_SOURCE_TEXT_CHARS) : text;
    }

    private static <T> void replace(Collection<T> target, Collection<T> values) {
        target.clear();
        target.addAll(values);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
