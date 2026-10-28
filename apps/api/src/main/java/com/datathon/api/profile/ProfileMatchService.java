package com.datathon.api.profile;

import static com.datathon.api.fundingcall.FundingCallSpecifications.notClosed;
import static com.datathon.api.fundingcall.FundingCallSpecifications.notExpiredOn;
import static com.datathon.api.fundingcall.FundingCallSpecifications.visible;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import com.datathon.api.source.SourceCatalog;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileMatchService {

    private final FundingCallRepository fundingCallRepository;
    private final MatchingService matchingService;
    private final SourceCatalog sourceCatalog;

    @Transactional(readOnly = true)
    public List<MatchResult> match(ProfileRequest profile) {
        LocalDate today = LocalDate.now();
        List<FundingCall> candidates = fundingCallRepository.findAll(visible().and(notClosed()).and(notExpiredOn(today)));
        return matchingService.match(profile, candidates, today).stream().map(this::toResult).toList();
    }

    private MatchResult toResult(MatchedCall matched) {
        FundingCall call = matched.fundingCall();
        return new MatchResult(
                FundingCallSummary.from(call, sourceCatalog.get(call.getSource())),
                matched.score(),
                matched.reasons(),
                matched.statusUnknown());
    }
}
