package com.datathon.api.fundingcall;

import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.dto.FundingCallDetail;
import com.datathon.api.fundingcall.dto.FundingCallExport;
import com.datathon.api.fundingcall.dto.FundingCallFacets;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/funding-calls")
@RequiredArgsConstructor
public class FundingCallController {

    static final String ATOM_XML = "application/atom+xml;charset=UTF-8";

    private final FundingCallService fundingCallService;
    private final AtomFeedWriter atomFeedWriter;

    @GetMapping
    PageResponse<FundingCallSummary> list(@Valid @ParameterObject FundingCallFilter filter) {
        return fundingCallService.list(filter);
    }

    @GetMapping("/facets")
    FundingCallFacets facets(@Valid @ParameterObject FundingCallFilter filter) {
        return fundingCallService.facets(filter);
    }

    @GetMapping("/export")
    FundingCallExport export(@Valid @ParameterObject FundingCallFilter filter) {
        return fundingCallService.export(filter);
    }

    @GetMapping(value = "/feed.atom", produces = ATOM_XML)
    ResponseEntity<String> feed(@Valid @ParameterObject FundingCallFilter filter, HttpServletRequest request) {
        String selfUrl = ServletUriComponentsBuilder.fromRequest(request).build().toUriString();
        String feed = atomFeedWriter.write(fundingCallService.newest(filter), selfUrl, Instant.now());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ATOM_XML)).body(feed);
    }

    @GetMapping("/{id}")
    FundingCallDetail get(@PathVariable Long id) {
        return fundingCallService.getById(id);
    }
}
