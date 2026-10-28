package com.datathon.api.ingest.extract;

import com.datathon.api.ingest.extract.CroatianDates.DateMatch;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DeadlineFinder {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;
    private static final int WINDOW_CHARS = 160;
    private static final int PERIOD_CONTEXT_CHARS = 80;

    private static final Pattern STRONG = Pattern.compile(
            "rok(?:a|om)?\\s+(?:je\\s+)?(?:za\\s+)?(?:podnošenj|prijav|dostav|predaj|zaprimanj)|rok:|"
                    + "(?:prijave|zahtjevi|prijedlozi)\\s+(?:se\\s+)?(?:podnose|zaprimaju|dostavljaju)[^.]{0,80}?\\s+do|"
                    + "datum zatvaranja",
            FLAGS);

    private static final Pattern WEAK = Pattern.compile(
            "zaključno\\s+(?:s|do|sa)|najkasnije\\s+do|otvoren\\s+do|traje\\s+do|"
                    + "do\\s+(?:dana\\s+)?(?=\\d{1,2}\\.\\s?(?:\\d{1,2}\\.|\\p{L}+\\s+\\d{4}))",
            FLAGS);

    private static final Pattern PERIOD_CONTEXT = Pattern.compile(
            "razdobl|nabav|provedb|isplat|ugovor|realizacij|prihvatljiv|troškov|vozil|plaćen|izvršen|završet",
            FLAGS);

    private static final Pattern PERIOD_BETWEEN = Pattern.compile(
            "isplat|razdobl|prihvatljiv|završet|provedb|realizacij", FLAGS);

    private static final Pattern UNTIL_FUNDS_USED = Pattern.compile(
            "do\\s+(?:utroška|iskorištenja|iskorištenosti)\\s+(?:raspoloživih\\s+|osiguranih\\s+)?sredstava", FLAGS);

    private DeadlineFinder() {
    }

    public static Optional<LocalDate> find(String text, LocalDate publishedAt) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Optional<LocalDate> strong = findAfter(STRONG, text, publishedAt, false);
        return strong.isPresent() ? strong : findAfter(WEAK, text, publishedAt, true);
    }

    public static Optional<String> findNote(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher matcher = UNTIL_FUNDS_USED.matcher(text);
        return matcher.find() ? Optional.of("do iskorištenja sredstava") : Optional.empty();
    }

    private static Optional<LocalDate> findAfter(Pattern keyword, String text, LocalDate publishedAt, boolean checkPeriod) {
        Matcher matcher = keyword.matcher(text);
        while (matcher.find()) {
            if (checkPeriod && isAboutAnotherPeriod(text, matcher.start())) {
                continue;
            }
            String window = text.substring(matcher.start(), Math.min(text.length(), matcher.end() + WINDOW_CHARS));
            for (DateMatch date : CroatianDates.findAll(window)) {
                if (PERIOD_BETWEEN.matcher(window.substring(0, date.position())).find()) {
                    break;
                }
                if (publishedAt == null || date.date().isAfter(publishedAt)) {
                    return Optional.of(date.date());
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isAboutAnotherPeriod(String text, int keywordStart) {
        String before = text.substring(Math.max(0, keywordStart - PERIOD_CONTEXT_CHARS), keywordStart);
        return PERIOD_CONTEXT.matcher(before).find();
    }
}
