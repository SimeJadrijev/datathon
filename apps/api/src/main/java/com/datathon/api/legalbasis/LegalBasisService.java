package com.datathon.api.legalbasis;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.LegalReference;
import com.datathon.api.regulation.GazetteCitationParser;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalBasisService {

    private static final Comparator<CitingFundingCall> OPEN_FIRST = Comparator
            .comparing((CitingFundingCall call) -> call.status() != FundingCallStatus.OPEN)
            .thenComparing(CitingFundingCall::deadline, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder()))
            .thenComparing(CitingFundingCall::id);

    private final RegulationRepository regulationRepository;
    private final FundingCallRepository fundingCallRepository;

    @Transactional(readOnly = true)
    public List<LegalBasis> forFundingCall(FundingCall fundingCall) {
        List<LegalReference> references = fundingCall.getLegalReferences();
        if (references.isEmpty()) {
            return List.of();
        }
        Set<String> gazetteNumbers = references.stream().map(LegalReference::getGazetteNumber).collect(Collectors.toSet());
        Map<String, List<Regulation>> regulationsByNumber = regulationRepository.findByGazetteNumberIn(gazetteNumbers)
                .stream()
                .collect(Collectors.groupingBy(Regulation::getGazetteNumber));
        return references.stream()
                .map(reference -> LegalBasis.of(reference, matchingRegulation(reference, regulationsByNumber)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitingFundingCall> citingFundingCalls(Regulation regulation) {
        return fundingCallRepository.findVisibleCitingGazetteNumber(regulation.getGazetteNumber()).stream()
                .filter(fundingCall -> cites(fundingCall, regulation))
                .map(CitingFundingCall::from)
                .sorted(OPEN_FIRST)
                .toList();
    }

    private static Regulation matchingRegulation(LegalReference reference, Map<String, List<Regulation>> byNumber) {
        return byNumber.getOrDefault(reference.getGazetteNumber(), List.of()).stream()
                .filter(regulation -> GazetteCitationParser.refersTo(reference.getCitedTitle(), regulation.getTitle()))
                .min(Comparator.comparingInt(regulation -> regulation.getTitle().length()))
                .orElse(null);
    }

    private static boolean cites(FundingCall fundingCall, Regulation regulation) {
        return fundingCall.getLegalReferences().stream()
                .anyMatch(reference -> reference.getGazetteNumber().equals(regulation.getGazetteNumber())
                        && GazetteCitationParser.refersTo(reference.getCitedTitle(), regulation.getTitle()));
    }
}
