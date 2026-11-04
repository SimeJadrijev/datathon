package com.datathon.api.profile;

import com.datathon.api.common.CountyNames;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.TargetGroup;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class MatchingService {

    static final int MAX_RESULTS = 20;

    private static final int STALE_UNKNOWN_STATUS_DAYS = 365;
    private static final int DEADLINE_SOON_DAYS = 30;
    private static final int TITLE_KEY_LENGTH = 80;
    private static final double MAIN_TYPE_POINTS = 4;
    private static final double EXTRA_TYPE_POINTS = 2;
    private static final double LOCAL_POINTS = 2;
    private static final double AREA_POINTS = 3;
    private static final int AREAS_COUNTED = 2;
    private static final double KEYWORD_POINTS = 3;
    private static final double UNKNOWN_TOPIC_POINTS = 1;
    private static final double ACTIVITY_POINTS = 4;
    private static final double OPEN_POINTS = 2;
    private static final double ANNOUNCED_POINTS = 1;
    private static final double DEADLINE_SOON_POINTS = 0.5;

    private static final Comparator<MatchedCall> BEST_FIRST = Comparator
            .comparingDouble(MatchedCall::score).reversed()
            .thenComparing(match -> match.fundingCall().getDeadline(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(match -> match.fundingCall().getId(), Comparator.nullsLast(Comparator.naturalOrder()));

    public List<MatchedCall> match(ProfileRequest profile, List<FundingCall> candidates, LocalDate today) {
        List<MatchedCall> bestFirst = candidates.stream()
                .map(call -> score(profile, call, today))
                .flatMap(Optional::stream)
                .sorted(BEST_FIRST)
                .toList();
        return withoutDuplicateTitles(bestFirst).stream().limit(MAX_RESULTS).toList();
    }

    private Optional<MatchedCall> score(ProfileRequest profile, FundingCall call, LocalDate today) {
        if (isClosedOrExpired(call, today) || isProbablyStale(call, today)) {
            return Optional.empty();
        }
        Scorecard scorecard = new Scorecard();
        if (!scoreTargetGroup(profile, call, scorecard)
                || !scoreCounty(profile, call, scorecard)
                || !scoreTopic(profile, call, scorecard)) {
            return Optional.empty();
        }
        scoreStatusAndDeadline(call, today, scorecard);
        return Optional.of(new MatchedCall(call, scorecard.points, scorecard.reasons, call.getStatus() == null));
    }

    private static boolean isClosedOrExpired(FundingCall call, LocalDate today) {
        return call.getStatus() == FundingCallStatus.CLOSED
                || (call.getDeadline() != null && call.getDeadline().isBefore(today));
    }

    private static boolean isProbablyStale(FundingCall call, LocalDate today) {
        if (call.getStatus() != null || call.getDeadline() != null) {
            return false;
        }
        LocalDate publishedAt = call.getPublishedAt();
        return publishedAt == null || publishedAt.isBefore(today.minusDays(STALE_UNKNOWN_STATUS_DAYS));
    }

    private static boolean scoreTargetGroup(ProfileRequest profile, FundingCall call, Scorecard scorecard) {
        if (call.getTargetGroups().isEmpty()) {
            return true;
        }
        Optional<TargetGroup> matched = Stream.concat(Stream.of(profile.type()), profile.additionalTypes().stream())
                .filter(call.getTargetGroups()::contains)
                .findFirst();
        matched.ifPresent(group -> scorecard.add(
                group == profile.type() ? MAIN_TYPE_POINTS : EXTRA_TYPE_POINTS,
                "ciljna skupina: " + MatchLabels.of(group)));
        return matched.isPresent();
    }

    private static boolean scoreCounty(ProfileRequest profile, FundingCall call, Scorecard scorecard) {
        Set<String> callCounties = call.getCounties();
        if (callCounties.isEmpty() || callCounties.stream().anyMatch(CountyNames::isWholeCountry)) {
            return true;
        }
        String wanted = CountyNames.normalize(profile.county());
        Optional<String> matched = callCounties.stream()
                .filter(county -> !wanted.isEmpty() && CountyNames.normalize(county).equals(wanted))
                .findFirst();
        matched.ifPresent(county -> scorecard.add(LOCAL_POINTS, "lokalno: " + county));
        return matched.isPresent();
    }

    private static boolean scoreTopic(ProfileRequest profile, FundingCall call, Scorecard scorecard) {
        List<Area> sharedAreas = profile.areas().stream().filter(call.getAreas()::contains).distinct().toList();
        if (!sharedAreas.isEmpty()) {
            scorecard.add(AREA_POINTS * Math.min(sharedAreas.size(), AREAS_COUNTED),
                    "područje: " + String.join(", ", sharedAreas.stream().map(MatchLabels::of).toList()));
        }
        List<String> titleKeywords = keywordsInTitle(profile, call);
        if (!titleKeywords.isEmpty()) {
            scorecard.add(KEYWORD_POINTS * titleKeywords.size(),
                    "ključne riječi u naslovu: " + String.join(", ", titleKeywords));
        }
        List<String> sharedDivisions = sharedActivityDivisions(profile, call);
        if (!sharedDivisions.isEmpty()) {
            scorecard.add(ACTIVITY_POINTS, "djelatnost NKD " + String.join(", ", sharedDivisions));
        }
        boolean topicMatched = !sharedAreas.isEmpty() || !titleKeywords.isEmpty() || !sharedDivisions.isEmpty();
        boolean topicUnknown = call.getAreas().isEmpty() && call.getActivityCodes().isEmpty();
        if (!topicMatched && topicUnknown && !call.getTargetGroups().isEmpty()) {
            scorecard.add(UNKNOWN_TOPIC_POINTS, "područje poziva nije navedeno");
            return true;
        }
        return topicMatched;
    }

    private static List<String> keywordsInTitle(ProfileRequest profile, FundingCall call) {
        String title = CountyNames.withoutDiacritics(call.getTitle().toLowerCase(Locale.ROOT));
        return profile.keywords().stream()
                .map(keyword -> keyword.strip().toLowerCase(Locale.ROOT))
                .filter(keyword -> !keyword.isEmpty() && title.contains(CountyNames.withoutDiacritics(keyword)))
                .distinct()
                .toList();
    }

    private static List<String> sharedActivityDivisions(ProfileRequest profile, FundingCall call) {
        return profile.activityCodes().stream()
                .map(MatchingService::division)
                .filter(division -> call.getActivityCodes().stream().anyMatch(code -> code.startsWith(division)))
                .distinct()
                .toList();
    }

    private static String division(String activityCode) {
        return activityCode.substring(0, 2);
    }

    private static void scoreStatusAndDeadline(FundingCall call, LocalDate today, Scorecard scorecard) {
        if (call.getStatus() == FundingCallStatus.OPEN) {
            scorecard.add(OPEN_POINTS, "otvoren");
        } else if (call.getStatus() == FundingCallStatus.ANNOUNCED) {
            scorecard.add(ANNOUNCED_POINTS, "najavljen");
        }
        LocalDate deadline = call.getDeadline();
        if (deadline != null && !deadline.isAfter(today.plusDays(DEADLINE_SOON_DAYS))) {
            scorecard.add(DEADLINE_SOON_POINTS, deadlineReason(ChronoUnit.DAYS.between(today, deadline)));
        }
    }

    private static String deadlineReason(long days) {
        if (days == 0) {
            return "rok istječe danas";
        }
        boolean singular = days % 10 == 1 && days % 100 != 11;
        return "rok za " + days + (singular ? " dan" : " dana");
    }

    private static List<MatchedCall> withoutDuplicateTitles(List<MatchedCall> bestFirst) {
        Set<String> seenKeys = new HashSet<>();
        List<MatchedCall> unique = new ArrayList<>();
        for (MatchedCall match : bestFirst) {
            if (seenKeys.add(titleKey(match.fundingCall().getTitle()))) {
                unique.add(match);
            }
        }
        return unique;
    }

    static String titleKey(String title) {
        String normalized = title.toLowerCase(Locale.ROOT).replaceAll("[^a-zčćšđž0-9]+", " ").strip();
        return normalized.substring(0, Math.min(TITLE_KEY_LENGTH, normalized.length()));
    }

    private static final class Scorecard {

        private double points;
        private final List<String> reasons = new ArrayList<>();

        void add(double additionalPoints, String reason) {
            points += additionalPoints;
            reasons.add(reason);
        }
    }
}
