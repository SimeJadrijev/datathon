package com.datathon.api.eligibility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private void addDeadlineReason(EligibilityContext context, List<EligibilityReason> reasons) {
        if (context.deadline() != null && context.deadline().isBefore(LocalDate.now())) {
            reasons.add(new EligibilityReason("Rok prijave (" + context.deadline().format(DATE) + ") je istekao.", null));
        }
    }

    private void addTargetGroupReason(EligibilityContext context, EligibilityCheckRequest request,
            List<EligibilityReason> reasons) {
        boolean isOtherGroup = request.targetGroup() != null && !context.targetGroups().isEmpty()
                && !context.targetGroups().contains(request.targetGroup());
        if (isOtherGroup) {
            reasons.add(new EligibilityReason("Poziv je namijenjen drugoj ciljnoj skupini: " + context.targetGroups() + ".", null));
        }
    }

    private void addCountyReason(EligibilityContext context, EligibilityCheckRequest request,
            List<EligibilityReason> reasons) {
        String county = request.county();
        boolean isNational = context.counties().isEmpty() || context.counties().contains("ALL");
        boolean isOtherCounty = county != null && !isNational
                && context.counties().stream().noneMatch(listed -> listed.equalsIgnoreCase(county));
        if (isOtherCounty) {
            reasons.add(new EligibilityReason("Poziv se odnosi na druga područja: " + context.counties() + ".", null));
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
