package com.datathon.api.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.source.SourceCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);

    private final MatchingService matchingService = new MatchingService();

    @Test
    void studentFromRijekaInterestedInEnvironmentGetsLocalAndScholarshipCalls() {
        var profile = new ProfileRequest(TargetGroup.STUDENT, List.of(TargetGroup.CITIZEN),
                List.of(Area.ENVIRONMENT, Area.EDUCATION), "Primorsko-goranska županija", List.of("stipendij"), List.of());
        FundingCall scholarship = call("Stipendije za studente", FundingCallStatus.OPEN, TargetGroup.STUDENT, Area.EDUCATION);
        FundingCall wrongGroup = call("Poticaj za tvrtke okoliš", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENVIRONMENT);
        FundingCall local = call("Gradska potpora okoliš", FundingCallStatus.OPEN, TargetGroup.CITIZEN, Area.ENVIRONMENT);
        local.getCounties().add("Primorsko-goranska");

        var matches = matchingService.match(profile, List.of(scholarship, wrongGroup, local), TODAY);

        assertThat(matches).extracting(match -> match.fundingCall().getTitle())
                .containsExactlyInAnyOrder("Stipendije za studente", "Gradska potpora okoliš");
        MatchedCall scholarshipMatch = matchOf(matches, "Stipendije za studente");
        assertThat(scholarshipMatch.reasons()).contains("ciljna skupina: studenti", "područje: obrazovanje",
                "ključne riječi u naslovu: stipendij", "otvoren");
        assertThat(scholarshipMatch.score()).isEqualTo(4 + 3 + 3 + 2);
        MatchedCall localMatch = matchOf(matches, "Gradska potpora okoliš");
        assertThat(localMatch.reasons()).contains("ciljna skupina: građani", "lokalno: Primorsko-goranska");
        assertThat(localMatch.score()).isEqualTo(2 + 2 + 3 + 2);
    }

    @Test
    void citizenWantingAnElectricCarMatchesOnKeywordsAndAreas() {
        var profile = new ProfileRequest(TargetGroup.CITIZEN, List.of(), List.of(Area.TRANSPORT, Area.ENERGY),
                "Grad Zagreb", List.of("vozil", "električn"), List.of());
        FundingCall vehicles = call("Sufinanciranje električnih vozila", FundingCallStatus.OPEN, TargetGroup.CITIZEN,
                Area.TRANSPORT, Area.ENERGY);
        FundingCall unrelated = call("Potpora za knjižnice", FundingCallStatus.OPEN, TargetGroup.CITIZEN, Area.CULTURE);

        var matches = matchingService.match(profile, List.of(vehicles, unrelated), TODAY);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().reasons()).contains("područje: promet, energetika", "ključne riječi u naslovu: vozil, električn");
        assertThat(matches.getFirst().score()).isEqualTo(4 + 6 + 6 + 2);
    }

    @Test
    void companyWithActivityCodeMatchesCallsForThatNkdDivision() {
        var profile = new ProfileRequest(TargetGroup.COMPANY, List.of(), List.of(), "Zagreb", List.of(), List.of("62.01"));
        FundingCall digital = call("Vaučeri za digitalizaciju", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        digital.getActivityCodes().add("62.09");
        FundingCall otherIndustry = call("Potpora za brodogradnju", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        otherIndustry.getActivityCodes().add("30.11");

        var matches = matchingService.match(profile, List.of(digital, otherIndustry), TODAY);

        assertThat(matches).extracting(match -> match.fundingCall().getTitle()).containsExactly("Vaučeri za digitalizaciju");
        assertThat(matches.getFirst().reasons()).contains("djelatnost NKD 62");
    }

    @Test
    void callWithoutATopicStillReachesItsTargetGroupInItsCounty() {
        var profile = new ProfileRequest(TargetGroup.ASSOCIATION, List.of(), List.of(Area.SOCIAL), "Primorsko-goranska",
                List.of("skolarin"), List.of());
        FundingCall smallGrants = call("Javni natječaj za male potpore u 2026.", FundingCallStatus.OPEN,
                TargetGroup.ASSOCIATION);
        smallGrants.getCounties().add("Primorsko-goranska");
        FundingCall fees = call("Sufinanciranje školarine", FundingCallStatus.OPEN, TargetGroup.ASSOCIATION, Area.EDUCATION);

        var matches = matchingService.match(profile, List.of(smallGrants, fees), TODAY);

        assertThat(matches).extracting(match -> match.fundingCall().getTitle())
                .containsExactlyInAnyOrder("Sufinanciranje školarine", "Javni natječaj za male potpore u 2026.");
        assertThat(matchOf(matches, "Javni natječaj za male potpore u 2026.").reasons())
                .contains("područje poziva nije navedeno");
    }

    @Test
    void closedAndExpiredCallsAreExcluded() {
        FundingCall closed = call("Zatvoren", FundingCallStatus.CLOSED, TargetGroup.COMPANY, Area.ENERGY);
        FundingCall expired = call("Istekao", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        expired.setDeadline(TODAY.minusDays(1));
        FundingCall endsToday = call("Istječe danas", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        endsToday.setDeadline(TODAY);

        var matches = matchingService.match(energyCompany(), List.of(closed, expired, endsToday), TODAY);

        assertThat(matches).extracting(match -> match.fundingCall().getTitle()).containsExactly("Istječe danas");
        assertThat(matches.getFirst().reasons()).contains("rok istječe danas");
    }

    @Test
    void localCallFromAnotherCountyIsExcludedButWholeCountryCallsStay() {
        FundingCall otherCounty = call("Istarska potpora", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        otherCounty.getCounties().add("Istarska");
        FundingCall wholeCountry = call("Državna potpora", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        wholeCountry.getCounties().add("Područje cijele Hrvatske");

        var matches = matchingService.match(energyCompany(), List.of(otherCounty, wholeCountry), TODAY);

        assertThat(matches).extracting(match -> match.fundingCall().getTitle()).containsExactly("Državna potpora");
    }

    @Test
    void countyMatchingIgnoresCountyWordAndCase() {
        var profile = new ProfileRequest(TargetGroup.COMPANY, List.of(), List.of(Area.ENERGY), "istarska županija",
                List.of(), List.of());
        FundingCall local = call("Istarska potpora", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        local.getCounties().add("Istarska");

        var matches = matchingService.match(profile, List.of(local), TODAY);

        assertThat(matches).hasSize(1);
    }

    @Test
    void callsWithTheSameNormalizedTitleAreMerged() {
        FundingCall first = call("Stipendije za studente 2026.", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        FundingCall duplicate = call("STIPENDIJE za studente 2026", FundingCallStatus.ANNOUNCED, TargetGroup.COMPANY, Area.ENERGY);

        var matches = matchingService.match(energyCompany(), List.of(duplicate, first), TODAY);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().fundingCall()).isSameAs(first);
    }

    @Test
    void unknownStatusIsFlaggedButNotPenalised() {
        FundingCall unknown = call("Bez statusa", null, TargetGroup.COMPANY, Area.ENERGY);
        unknown.setPublishedAt(TODAY.minusDays(30));
        FundingCall open = call("S statusom", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);

        var matches = matchingService.match(energyCompany(), List.of(unknown, open), TODAY);

        MatchedCall unknownMatch = matchOf(matches, "Bez statusa");
        assertThat(unknownMatch.statusUnknown()).isTrue();
        assertThat(unknownMatch.score()).isEqualTo(4 + 3);
        assertThat(matchOf(matches, "S statusom").statusUnknown()).isFalse();
    }

    @Test
    void unknownStatusWithoutDeadlineAndOldOrUndatedPublicationIsExcluded() {
        FundingCall old = call("Star", null, TargetGroup.COMPANY, Area.ENERGY);
        old.setPublishedAt(TODAY.minusDays(366));
        FundingCall undated = call("Bez datuma", null, TargetGroup.COMPANY, Area.ENERGY);

        assertThat(matchingService.match(energyCompany(), List.of(old, undated), TODAY)).isEmpty();
    }

    @Test
    void callWithoutTargetGroupsStillMatchesOnTopic() {
        FundingCall open = call("Opća potpora", FundingCallStatus.OPEN, null, Area.ENERGY);

        var matches = matchingService.match(energyCompany(), List.of(open), TODAY);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().reasons()).doesNotContain("ciljna skupina: tvrtke");
    }

    @Test
    void resultsAreSortedByScoreThenEarliestDeadlineAndCappedAtTwenty() {
        List<FundingCall> calls = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            calls.add(call("Poziv broj " + i, FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY));
        }
        FundingCall strong = call("Jači poziv", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY, Area.ENTREPRENEURSHIP);
        calls.add(strong);
        var profile = new ProfileRequest(TargetGroup.COMPANY, List.of(), List.of(Area.ENERGY, Area.ENTREPRENEURSHIP), "Zagreb",
                List.of(), List.of());

        var matches = matchingService.match(profile, calls, TODAY);

        assertThat(matches).hasSize(MatchingService.MAX_RESULTS);
        assertThat(matches.getFirst().fundingCall()).isSameAs(strong);
    }

    @Test
    void earlierDeadlineWinsATie() {
        FundingCall later = call("Kasniji rok", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        later.setDeadline(TODAY.plusDays(90));
        FundingCall earlier = call("Raniji rok", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        earlier.setDeadline(TODAY.plusDays(60));

        var matches = matchingService.match(energyCompany(), List.of(later, earlier), TODAY);

        assertThat(matches).extracting(match -> match.fundingCall().getTitle()).containsExactly("Raniji rok", "Kasniji rok");
    }

    @Test
    void deadlineWithinThirtyDaysAddsABonusAndAReason() {
        FundingCall soon = call("Uskoro", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        soon.setDeadline(TODAY.plusDays(12));

        var matches = matchingService.match(energyCompany(), List.of(soon), TODAY);

        assertThat(matches.getFirst().reasons()).contains("rok za 12 dana");
        assertThat(matches.getFirst().score()).isEqualTo(4 + 3 + 2 + 0.5);
    }

    @Test
    void deadlineReasonUsesCorrectCroatianPlural() {
        FundingCall oneDay = call("Za jedan dan", FundingCallStatus.OPEN, TargetGroup.COMPANY, Area.ENERGY);
        oneDay.setDeadline(TODAY.plusDays(1));

        var matches = matchingService.match(energyCompany(), List.of(oneDay), TODAY);

        assertThat(matches.getFirst().reasons()).contains("rok za 1 dan");
    }

    @Test
    void titleKeyCutsAtEightyCharactersAndDropsPunctuation() {
        assertThat(MatchingService.titleKey("  Čaj, kava & kolači!!  ")).isEqualTo("čaj kava kolači");
        assertThat(MatchingService.titleKey("a".repeat(120))).hasSize(80);
    }

    private static ProfileRequest energyCompany() {
        return new ProfileRequest(TargetGroup.COMPANY, List.of(), List.of(Area.ENERGY), "Zagreb", List.of(), List.of());
    }

    private static MatchedCall matchOf(List<MatchedCall> matches, String title) {
        return matches.stream().filter(match -> match.fundingCall().getTitle().equals(title)).findFirst().orElseThrow();
    }

    private static FundingCall call(String title, FundingCallStatus status, TargetGroup targetGroup, Area... areas) {
        var call = new FundingCall(SourceCode.FZOEU, "id-" + title, title);
        call.setStatus(status);
        if (targetGroup != null) {
            call.getTargetGroups().add(targetGroup);
        }
        call.getAreas().addAll(List.of(areas));
        return call;
    }
}
