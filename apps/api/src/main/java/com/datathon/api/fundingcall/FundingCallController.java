package com.datathon.api.fundingcall;

import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.dto.FundingCallDetail;
import com.datathon.api.fundingcall.dto.FundingCallFacets;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funding-calls")
@RequiredArgsConstructor
public class FundingCallController {

    private final FundingCallService fundingCallService;

    @GetMapping
    PageResponse<FundingCallSummary> list(@Valid @ParameterObject FundingCallFilter filter) {
        return fundingCallService.list(filter);
    }

    @GetMapping("/facets")
    FundingCallFacets facets(@Valid @ParameterObject FundingCallFilter filter) {
        return fundingCallService.facets(filter);
    }

    @GetMapping("/{id}")
    FundingCallDetail get(@PathVariable Long id) {
        return fundingCallService.getById(id);
    }
}
