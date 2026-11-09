package com.datathon.api.fundingcall;

import com.datathon.api.source.SourceCode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
public class DuplicateMarker implements ApplicationRunner {

    private static final Map<SourceCode, Integer> REPOSTING_SOURCES = Map.of(
            SourceCode.AGENCIES, 1,
            SourceCode.GOV_HR_NEWS, 2,
            SourceCode.UNIVERSITIES, 3,
            SourceCode.LOCAL_GOVERNMENT, 4);
    private static final Set<SourceCode> SOURCES_WITH_HOST_IN_ID = REPOSTING_SOURCES.keySet();
    private static final Pattern NOT_A_LETTER_OR_DIGIT = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern LEADING_PUBLISHED = Pattern.compile("^objavljen[aoi]? (je |su )?");
    private static final int MAX_PUBLISHED_DAYS_APART = 30;
    private static final Comparator<FundingCall> PRIMARY_FIRST = Comparator
            .comparingInt((FundingCall call) -> REPOSTING_SOURCES.getOrDefault(call.getSource(), 0))
            .thenComparing(FundingCall::getId);

    private final FundingCallRepository fundingCallRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        markDuplicates();
    }

    @Transactional
    public void markDuplicates() {
        Map<String, List<FundingCall>> byTitle = new LinkedHashMap<>();
        for (FundingCall call : fundingCallRepository.findAll()) {
            call.setDuplicateOf(null);
            if (!call.isHidden()) {
                byTitle.computeIfAbsent(titleKey(call.getTitle()), key -> new ArrayList<>()).add(call);
            }
        }
        int marked = 0;
        for (List<FundingCall> sameTitle : byTitle.values()) {
            marked += markWithinGroup(sameTitle);
        }
        if (marked > 0) {
            log.info("Marked {} funding calls as duplicates of another call (other source or same page)", marked);
        }
    }

    private static int markWithinGroup(List<FundingCall> sameTitle) {
        if (sameTitle.size() < 2) {
            return 0;
        }
        List<FundingCall> ordered = sameTitle.stream().sorted(PRIMARY_FIRST).toList();
        List<FundingCall> kept = new ArrayList<>();
        int marked = 0;
        for (FundingCall call : ordered) {
            FundingCall primary = kept.stream().filter(candidate -> isSameCall(candidate, call)).findFirst().orElse(null);
            if (primary == null) {
                kept.add(call);
            } else {
                call.setDuplicateOf(primary.getId());
                marked++;
            }
        }
        return marked;
    }

    static boolean isSameCall(FundingCall first, FundingCall second) {
        if (isSameUrl(first, second)) {
            return true;
        }
        if (origin(first).equals(origin(second))) {
            return false;
        }
        if (first.getDeadline() != null && second.getDeadline() != null && !first.getDeadline().equals(second.getDeadline())) {
            return false;
        }
        return isPublishedClose(first.getPublishedAt(), second.getPublishedAt());
    }

    private static boolean isSameUrl(FundingCall first, FundingCall second) {
        return first.getUrl() != null && !first.getUrl().isBlank() && first.getUrl().equals(second.getUrl());
    }

    private static boolean isPublishedClose(LocalDate first, LocalDate second) {
        if (first == null || second == null) {
            return true;
        }
        return Math.abs(ChronoUnit.DAYS.between(first, second)) <= MAX_PUBLISHED_DAYS_APART;
    }

    private static String origin(FundingCall call) {
        String externalId = Objects.requireNonNullElse(call.getExternalId(), "");
        int separator = externalId.indexOf(':');
        boolean hasHost = SOURCES_WITH_HOST_IN_ID.contains(call.getSource()) && separator > 0;
        return call.getSource() + (hasHost ? "/" + externalId.substring(0, separator) : "");
    }

    static String titleKey(String title) {
        String folded = DIACRITICS.matcher(Normalizer.normalize(title == null ? "" : title, Normalizer.Form.NFD))
                .replaceAll("");
        String words = NOT_A_LETTER_OR_DIGIT.matcher(folded.toLowerCase(Locale.ROOT)).replaceAll(" ").strip();
        return LEADING_PUBLISHED.matcher(words).replaceFirst("");
    }
}
