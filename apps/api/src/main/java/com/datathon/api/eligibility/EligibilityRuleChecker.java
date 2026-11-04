package com.datathon.api.eligibility;

import com.datathon.api.common.CountyNames;
import com.datathon.api.profile.MatchLabels;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Fallback when the model is not available. Plain rules over the fields of the call and the optional profile.
 * It never gives more than UNCLEAR, because rules over a few fields cannot say a person qualifies.
 */
@Component
public class EligibilityRuleChecker {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy.");

    public EligibilityCheckResponse check(EligibilityContext context, Map<String, Object> answers,
            EligibilityCheckRequest request) {
        List<EligibilityReason> reasons = new ArrayList<>();
        addDeadlineReason(context, reasons);
        addTargetGroupReason(context, request, reasons);
        addCountyReason(context, request, reasons);
        return new EligibilityCheckResponse(EligibilityVerdict.UNCLEAR, reasons, missingInfo(context, answers),
                EligibilityCheckResponse.DISCLAIMER, context.url());
    }

    /**
     * Whatever the answers are, nobody can apply after the deadline, so the model is not asked and the answer
     * is never LIKELY_ELIGIBLE (it would contradict the "deadline passed" notice on the detail page).
     */
    public EligibilityCheckResponse expired(EligibilityContext context) {
        List<EligibilityReason> reasons = new ArrayList<>();
        addDeadlineReason(context, reasons);
        return new EligibilityCheckResponse(EligibilityVerdict.LIKELY_NOT_ELIGIBLE, reasons,
                List.of("Provjerite kod davatelja hoće li poziv biti ponovno raspisan."),
                EligibilityCheckResponse.DISCLAIMER, context.url());
    }

    private void addDeadlineReason(EligibilityContext context, List<EligibilityReason> reasons) {
        if (context.isDeadlinePassed(LocalDate.now())) {
            reasons.add(new EligibilityReason("Rok prijave (" + context.deadline().format(DATE)
                    + ") je istekao, prijava više nije moguća.", null));
        } else if (context.closed()) {
            reasons.add(new EligibilityReason("Poziv je zatvoren, prijava više nije moguća.", null));
        }
    }

    private void addTargetGroupReason(EligibilityContext context, EligibilityCheckRequest request,
            List<EligibilityReason> reasons) {
        boolean isOtherGroup = request.targetGroup() != null && !context.targetGroups().isEmpty()
                && !context.targetGroups().contains(request.targetGroup());
        if (isOtherGroup) {
            String groups = context.targetGroups().stream().map(MatchLabels::of).sorted().collect(Collectors.joining(", "));
            reasons.add(new EligibilityReason("Poziv je namijenjen drugoj ciljnoj skupini: " + groups + ".", null));
        }
    }

    private void addCountyReason(EligibilityContext context, EligibilityCheckRequest request,
            List<EligibilityReason> reasons) {
        String county = request.county();
        boolean isNational = context.counties().isEmpty() || context.counties().stream().anyMatch(CountyNames::isWholeCountry);
        boolean isOtherCounty = county != null && !county.isBlank() && !isNational && context.counties().stream()
                .noneMatch(listed -> CountyNames.normalize(listed).equals(CountyNames.normalize(county)));
        if (isOtherCounty) {
            reasons.add(new EligibilityReason("Poziv se odnosi na druga područja: "
                    + String.join(", ", context.counties()) + ".", null));
        }
    }

    private List<String> missingInfo(EligibilityContext context, Map<String, Object> answers) {
        List<String> missing = new ArrayList<>();
        if (!context.questions().isEmpty() && answers.size() < context.questions().size()) {
            missing.add("Odgovorite na sva pitanja o uvjetima poziva.");
        }
        missing.add("Detaljna procjena trenutno nije dostupna, prikazani su samo osnovni podaci o pozivu.");
        return missing;
    }
}
