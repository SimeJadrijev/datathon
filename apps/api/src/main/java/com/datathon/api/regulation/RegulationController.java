package com.datathon.api.regulation;

import com.datathon.api.common.PageResponse;
import com.datathon.api.regulation.dto.RegulationDetail;
import com.datathon.api.regulation.dto.RegulationFacets;
import com.datathon.api.regulation.dto.RegulationFilter;
import com.datathon.api.regulation.dto.RegulationSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regulations")
@RequiredArgsConstructor
public class RegulationController {

    private final RegulationService regulationService;

    @GetMapping
    PageResponse<RegulationSummary> list(@Valid @ParameterObject RegulationFilter filter) {
        return regulationService.list(filter);
    }

    @GetMapping("/facets")
    RegulationFacets facets(@Valid @ParameterObject RegulationFilter filter) {
        return regulationService.facets(filter);
    }

    @GetMapping("/{id}")
    RegulationDetail get(@PathVariable Long id) {
        return regulationService.getById(id);
    }
}
