package com.datathon.api.eligibility;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.TargetGroup;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EligibilityRuleCheckerTest {

    private final EligibilityRuleChecker checker = new EligibilityRuleChecker();

    @Test
    void reasonsUseCroatianNamesAndCountySpellingsMatch() {
        EligibilityContext context = context(false, Set.of(TargetGroup.COMPANY), Set.of("Splitsko-dalmatinska"));

        var response = checker.check(context, Map.of(),
                new EligibilityCheckRequest(Map.of(), TargetGroup.ASSOCIATION, "Splitsko-dalmatinska županija"));

        assertThat(response.reasons()).extracting(EligibilityReason::text)
                .containsExactly("Poziv je namijenjen drugoj ciljnoj skupini: tvrtke.");
    }

    @Test
    void wholeCountryWrittenOutIsNational() {
        EligibilityContext context = context(false, Set.of(), Set.of("Područje cijele Hrvatske"));

        var response = checker.check(context, Map.of(), new EligibilityCheckRequest(Map.of(), null, "Istarska"));

        assertThat(response.reasons()).isEmpty();
    }

    @Test
    void closedCallWithoutDeadlineIsReportedAsClosed() {
        var response = checker.expired(context(true, Set.of(), Set.of()));

        assertThat(response.verdict()).isEqualTo(EligibilityVerdict.LIKELY_NOT_ELIGIBLE);
        assertThat(response.reasons()).extracting(EligibilityReason::text)
                .containsExactly("Poziv je zatvoren, prijava više nije moguća.");
    }

    private static EligibilityContext context(boolean closed, Set<TargetGroup> groups, Set<String> counties) {
        return new EligibilityContext(1L, "Poziv", "https://example.hr", "", null, closed, groups, counties, List.of());
    }
}
