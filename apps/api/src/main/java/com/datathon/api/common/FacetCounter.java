package com.datathon.api.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FacetCounter {

    public static final String UNKNOWN_VALUE = "UNKNOWN";

    private final EntityManager entityManager;

    public <E> List<FacetCount> countByColumn(Class<E> entity, Specification<E> specification, String attribute) {
        return count(entity, specification, root -> root.get(attribute), false);
    }

    public <E> List<FacetCount> countByColumnWithUnknown(Class<E> entity, Specification<E> specification, String attribute) {
        return count(entity, specification, root -> root.get(attribute), true);
    }

    public <E> List<FacetCount> countByElement(Class<E> entity, Specification<E> specification, String collectionAttribute) {
        return count(entity, specification, root -> root.join(collectionAttribute), false);
    }

    private <E> List<FacetCount> count(Class<E> entity, Specification<E> specification,
                                       Function<Root<E>, Expression<?>> valueOf, boolean includeUnknown) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<E> root = query.from(entity);
        Expression<?> value = valueOf.apply(root);
        query.multiselect(value, criteriaBuilder.countDistinct(root)).groupBy(value);
        Predicate restriction = specification.toPredicate(root, query, criteriaBuilder);
        if (restriction != null) {
            query.where(restriction);
        }

        Map<String, Long> countsByValue = new LinkedHashMap<>();
        for (Tuple row : entityManager.createQuery(query).getResultList()) {
            if (row.get(0) != null || includeUnknown) {
                countsByValue.merge(label(row.get(0)), row.get(1, Long.class), Long::sum);
            }
        }
        return FacetCount.sortedByCount(countsByValue);
    }

    private static String label(Object value) {
        return switch (value) {
            case null -> UNKNOWN_VALUE;
            case Enum<?> enumValue -> enumValue.name();
            default -> value.toString();
        };
    }
}
