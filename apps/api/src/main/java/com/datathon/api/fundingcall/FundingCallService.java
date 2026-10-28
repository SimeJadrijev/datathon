package com.datathon.api.fundingcall;

import static com.datathon.api.fundingcall.FundingCallSpecifications.hasId;
import static com.datathon.api.fundingcall.FundingCallSpecifications.matching;
import static com.datathon.api.fundingcall.FundingCallSpecifications.matchingExcept;
import static com.datathon.api.fundingcall.FundingCallSpecifications.visible;

import com.datathon.api.common.CountyNames;
import com.datathon.api.common.FacetCount;
import com.datathon.api.common.FacetCounter;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.dto.FundingCallDetail;
import com.datathon.api.fundingcall.dto.FundingCallFacets;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import com.datathon.api.source.SourceCatalog;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingCallService {

    private final FundingCallRepository fundingCallRepository;
    private final SourceCatalog sourceCatalog;
    private final FacetCounter facetCounter;

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

    @Transactional(readOnly = true)
    public FundingCallFacets facets(FundingCallFilter filter) {
        Set<String> countyValues = countyValuesFor(filter);
        return new FundingCallFacets(
                facetCounter.countByColumnWithUnknown(FundingCall.class,
                        specificationIgnoring(filter, countyValues, FundingCallFacet.STATUS), "status"),
                facetByElement(filter, countyValues, FundingCallFacet.TARGET_GROUP, "targetGroups"),
                facetByElement(filter, countyValues, FundingCallFacet.AREA, "areas"),
                mergeCountySpellings(facetByElement(filter, countyValues, FundingCallFacet.COUNTY, "counties")),
                facetByColumn(filter, countyValues, FundingCallFacet.SOURCE, "source"),
                facetByColumn(filter, countyValues, FundingCallFacet.TYPE, "type"));
    }

    private List<FacetCount> facetByColumn(FundingCallFilter filter, Set<String> countyValues, FundingCallFacet facet,
                                           String attribute) {
        return facetCounter.countByColumn(FundingCall.class, specificationIgnoring(filter, countyValues, facet), attribute);
    }

    private List<FacetCount> facetByElement(FundingCallFilter filter, Set<String> countyValues, FundingCallFacet facet,
                                            String collectionAttribute) {
        return facetCounter.countByElement(FundingCall.class, specificationIgnoring(filter, countyValues, facet),
                collectionAttribute);
    }

    private static Specification<FundingCall> specificationIgnoring(FundingCallFilter filter, Set<String> countyValues,
                                                                    FundingCallFacet facet) {
        return visible().and(matchingExcept(filter, countyValues, facet));
    }

    private static List<FacetCount> mergeCountySpellings(List<FacetCount> counties) {
        Map<String, List<FacetCount>> spellingsByName = counties.stream()
                .collect(Collectors.groupingBy(county -> CountyNames.normalize(county.value())));
        Map<String, Long> countsByLabel = new HashMap<>();
        spellingsByName.forEach((name, spellings) -> {
            String label = FundingCall.ALL_COUNTIES.equals(name)
                    ? FundingCall.ALL_COUNTIES
                    : spellings.stream().max(Comparator.comparingLong(FacetCount::count)).orElseThrow().value();
            countsByLabel.put(label, spellings.stream().mapToLong(FacetCount::count).sum());
        });
        return FacetCount.sortedByCount(countsByLabel);
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
