package com.datathon.api.common;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class TextSearch {

    private static final char LIKE_ESCAPE = '\\';

    private TextSearch() {
    }

    public static String containsPattern(String text) {
        return "%" + text.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    public static Predicate likeIgnoringCaseAndAccents(CriteriaBuilder cb, Expression<String> column, String pattern) {
        Expression<String> normalizedColumn = cb.lower(cb.function("unaccent", String.class, column));
        Expression<String> normalizedPattern = cb.lower(cb.function("unaccent", String.class, cb.literal(pattern)));
        return cb.like(normalizedColumn, normalizedPattern, LIKE_ESCAPE);
    }
}
