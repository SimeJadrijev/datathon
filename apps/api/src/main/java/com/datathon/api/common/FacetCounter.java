package com.datathon.api.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
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
        return count(entity, specification, root -> root.get(attribute));
    }

    public <E> List<FacetCount> countByElement(Class<E> entity, Specification<E> specification, String collectionAttribute) {
        return count(entity, specification, root -> root.join(collectionAttribute));
    }

    private <E> List<FacetCount> count(Class<E> entity, Specification<E> specification,
                                       Function<Root<E>, Expression<?>> valueOf) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<E> root = query.from(entity);
        Expression<?> value = valueOf.apply(root);
        query.multiselect(value, criteriaBuilder.count(root))
                .where(specification.toPredicate(root, query, criteriaBuilder))
                .groupBy(value);

        Map<String, Long> countsByValue = new LinkedHashMap<>();
        for (Tuple row : entityManager.createQuery(query).getResultList()) {
            countsByValue.merge(label(row.get(0)), row.get(1, Long.class), Long::sum);
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
