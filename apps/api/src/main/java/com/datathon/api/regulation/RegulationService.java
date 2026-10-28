package com.datathon.api.regulation;

import static com.datathon.api.regulation.RegulationSpecifications.matching;
import static com.datathon.api.regulation.RegulationSpecifications.matchingExcept;

import com.datathon.api.common.FacetCounter;
import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.regulation.dto.RegulationDetail;
import com.datathon.api.regulation.dto.RegulationFacets;
import com.datathon.api.regulation.dto.RegulationFilter;
import com.datathon.api.regulation.dto.RegulationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegulationService {

    private static final Sort NEWEST_FIRST =
            Sort.by(Sort.Order.desc("publishedAt").nullsLast(), Sort.Order.desc("id"));

    private final RegulationRepository regulationRepository;
    private final FacetCounter facetCounter;

    @Transactional(readOnly = true)
    public PageResponse<RegulationSummary> list(RegulationFilter filter) {
        rejectInvertedPublicationRange(filter);
        Pageable pageable = PageRequests.of(filter.page(), filter.size(), NEWEST_FIRST);
        return PageResponse.from(regulationRepository.findAll(matching(filter), pageable), RegulationSummary::from);
    }

    @Transactional(readOnly = true)
    public RegulationDetail getById(Long id) {
        return regulationRepository.findById(id)
                .map(RegulationDetail::from)
                .orElseThrow(() -> new NotFoundException("Propis ne postoji."));
    }

    @Transactional(readOnly = true)
    public RegulationFacets facets(RegulationFilter filter) {
        rejectInvertedPublicationRange(filter);
        return new RegulationFacets(
                facetCounter.countByColumn(Regulation.class, matchingExcept(filter, RegulationFacet.TYPE), "type"),
                facetCounter.countByElement(Regulation.class, matchingExcept(filter, RegulationFacet.AREA), "areas"));
    }

    private static void rejectInvertedPublicationRange(RegulationFilter filter) {
        boolean isInverted = filter.publishedFrom() != null && filter.publishedTo() != null
                && filter.publishedFrom().isAfter(filter.publishedTo());
        if (isInverted) {
            throw new InvalidRequestException("publishedFrom: ne smije biti nakon publishedTo");
        }
    }
}
