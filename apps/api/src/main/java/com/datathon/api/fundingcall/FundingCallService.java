package com.datathon.api.fundingcall;

import static com.datathon.api.fundingcall.FundingCallSpecifications.hasId;
import static com.datathon.api.fundingcall.FundingCallSpecifications.matching;
import static com.datathon.api.fundingcall.FundingCallSpecifications.visible;

import com.datathon.api.common.CountyNames;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.dto.FundingCallDetail;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import com.datathon.api.source.SourceCatalog;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingCallService {

    private final FundingCallRepository fundingCallRepository;
    private final SourceCatalog sourceCatalog;

    @Transactional(readOnly = true)
    public PageResponse<FundingCallSummary> list(FundingCallFilter filter) {
        Pageable pageable = PageRequests.of(filter.page(), filter.size(), filter.sort().toSort());
        var specification = visible().and(matching(filter, countyValuesFor(filter)));
        return PageResponse.from(fundingCallRepository.findAll(specification, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public FundingCallDetail getById(Long id) {
        FundingCall fundingCall = fundingCallRepository.findOne(visible().and(hasId(id)))
                .orElseThrow(() -> new NotFoundException("Natječaj ne postoji."));
        return FundingCallDetail.from(fundingCall, sourceCatalog.get(fundingCall.getSource()));
    }

    private Set<String> countyValuesFor(FundingCallFilter filter) {
        if (filter.county() == null) {
            return Set.of();
        }
        return CountyNames.storedValuesMatching(filter.county(), fundingCallRepository.findDistinctCounties());
    }

    private FundingCallSummary toSummary(FundingCall fundingCall) {
        return FundingCallSummary.from(fundingCall, sourceCatalog.get(fundingCall.getSource()));
    }
}
