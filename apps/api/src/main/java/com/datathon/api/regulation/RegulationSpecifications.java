package com.datathon.api.regulation;

import com.datathon.api.common.TextSearch;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.dto.RegulationFilter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class RegulationSpecifications {

    private RegulationSpecifications() {
    }

    public static Specification<Regulation> matching(RegulationFilter filter) {
        return matchingExcept(filter, null);
    }

    public static Specification<Regulation> matchingExcept(RegulationFilter filter, RegulationFacet ignoredFacet) {
        List<Specification<Regulation>> parts = new ArrayList<>();
        parts.add(titleContains(filter.q()));
        parts.add(ignoredFacet == RegulationFacet.TYPE ? null : ofType(filter.type()));
        parts.add(ignoredFacet == RegulationFacet.AREA ? null : hasArea(filter.area()));
        parts.add(publishedOnOrAfter(filter.publishedFrom()));
        parts.add(publishedOnOrBefore(filter.publishedTo()));
        return Specification.allOf(parts.stream().filter(part -> part != null).toList());
    }

    public static Specification<Regulation> hasId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    private static Specification<Regulation> titleContains(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = TextSearch.containsPattern(text);
        return (root, query, cb) -> TextSearch.likeIgnoringCaseAndAccents(cb, root.get("title"), pattern);
    }

    private static Specification<Regulation> ofType(RegulationType type) {
        return type == null ? null : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private static Specification<Regulation> hasArea(Area area) {
        return area == null ? null : (root, query, cb) -> cb.isMember(area, root.get("areas"));
    }

    private static Specification<Regulation> publishedOnOrAfter(LocalDate date) {
        return date == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("publishedAt"), date);
    }

    private static Specification<Regulation> publishedOnOrBefore(LocalDate date) {
        return date == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("publishedAt"), date);
    }
}
