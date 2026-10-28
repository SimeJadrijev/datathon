package com.datathon.api.fundingcall;

import com.datathon.api.common.TextSearch;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallStatusFilter;
import com.datathon.api.source.SourceCode;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class FundingCallSpecifications {

    private FundingCallSpecifications() {
    }

    public static Specification<FundingCall> visible() {
        return (root, query, cb) -> cb.and(cb.isFalse(root.get("hidden")), cb.isNull(root.get("duplicateOf")));
    }

    public static Specification<FundingCall> hasId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<FundingCall> matching(FundingCallFilter filter, Collection<String> countyValues) {
        return matchingExcept(filter, countyValues, null);
    }

    public static Specification<FundingCall> matchingExcept(FundingCallFilter filter, Collection<String> countyValues,
                                                            FundingCallFacet ignoredFacet) {
        List<Specification<FundingCall>> parts = new ArrayList<>();
        parts.add(titleOrProviderContains(filter.q()));
        parts.add(ignoredFacet == FundingCallFacet.STATUS ? null : hasStatus(filter.status()));
        parts.add(ignoredFacet == FundingCallFacet.TARGET_GROUP ? null : hasTargetGroup(filter.targetGroup()));
        parts.add(ignoredFacet == FundingCallFacet.AREA ? null : hasArea(filter.area()));
        parts.add(ignoredFacet == FundingCallFacet.COUNTY || filter.county() == null ? null : inAnyCounty(countyValues));
        parts.add(ignoredFacet == FundingCallFacet.SOURCE ? null : fromSource(filter.source()));
        parts.add(ignoredFacet == FundingCallFacet.TYPE ? null : ofType(filter.type()));
        parts.add(deadlineOnOrBefore(filter.deadlineBefore()));
        return Specification.allOf(parts.stream().filter(part -> part != null).toList());
    }

    public static Specification<FundingCall> titleOrProviderContains(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = TextSearch.containsPattern(text);
        return (root, query, cb) -> cb.or(
                TextSearch.likeIgnoringCaseAndAccents(cb, root.get("title"), pattern),
                TextSearch.likeIgnoringCaseAndAccents(cb, root.get("provider"), pattern));
    }

    public static Specification<FundingCall> hasStatus(FundingCallStatusFilter status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> status.isUnknown()
                ? cb.isNull(root.get("status"))
                : cb.equal(root.get("status"), status.status());
    }

    public static Specification<FundingCall> hasTargetGroup(TargetGroup targetGroup) {
        return targetGroup == null ? null : (root, query, cb) -> cb.isMember(targetGroup, root.get("targetGroups"));
    }

    public static Specification<FundingCall> hasArea(Area area) {
        return area == null ? null : (root, query, cb) -> cb.isMember(area, root.get("areas"));
    }

    public static Specification<FundingCall> inAnyCounty(Collection<String> countyValues) {
        if (countyValues == null) {
            return null;
        }
        return (root, query, cb) -> {
            Predicate[] anyCounty = countyValues.stream()
                    .map(county -> cb.isMember(county, root.<Collection<String>>get("counties")))
                    .toArray(Predicate[]::new);
            return anyCounty.length == 0 ? cb.disjunction() : cb.or(anyCounty);
        };
    }

    public static Specification<FundingCall> fromSource(SourceCode source) {
        return source == null ? null : (root, query, cb) -> cb.equal(root.get("source"), source);
    }

    public static Specification<FundingCall> ofType(FundingCallType type) {
        return type == null ? null : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<FundingCall> deadlineOnOrBefore(LocalDate date) {
        return date == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("deadline"), date);
    }
}
