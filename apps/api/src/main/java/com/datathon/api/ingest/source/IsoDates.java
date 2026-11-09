package com.datathon.api.ingest.source;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

final class IsoDates {

    private static final int DATE_CHARS = 10;

    private IsoDates() {
    }

    static LocalDate leading(String dateTime) {
        if (dateTime == null || dateTime.length() < DATE_CHARS) {
            return null;
        }
        try {
            return LocalDate.parse(dateTime.substring(0, DATE_CHARS));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
