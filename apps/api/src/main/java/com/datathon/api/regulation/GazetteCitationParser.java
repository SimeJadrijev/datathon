package com.datathon.api.regulation;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GazetteCitationParser {

    public record Citation(String citedTitle, String gazetteNumber) {
    }

    public static final String ISSUE_URL = "https://narodne-novine.nn.hr/eli/sluzbeni/%s/%s";

    private static final int FLAGS = Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;
    private static final int MAX_CITATIONS = 60;
    private static final int TITLE_WINDOW_CHARS = 250;
    private static final int NUMBERS_WINDOW_CHARS = 220;
    private static final int MAX_TITLE_CHARS = 300;
    private static final int SUBJECT_CHARS = 80;
    private static final int MIN_SUBJECT_CHARS = 4;

    private static final Pattern MARKER = Pattern.compile(
            "[„“\"»”']?\\s*(?:Narodn(?:e|ih|im)\\s+novin(?:e|a|ama)|\\bNN\\b)\\s*[“”\"«']?", FLAGS);
    private static final Pattern ISSUE = Pattern.compile("(?<![\\d/.-])(\\d{1,3})\\s?/\\s?(\\d{4}|\\d{2})(?![\\d/-])");
    private static final Pattern WINDOW_END = Pattern.compile("[);\\n]|\\s(?:od|kojim|kojom|u daljnjem)\\s");
    private static final Pattern ACT = Pattern.compile(
            "(?<!\\p{L})(?:Zakon|Uredb|Pravilnik|Odluk|Naredb|Program|Statut|Strategij|Kodeks|Strateškog plana|Strateški plan|"
                    + "Nacionalnog plana|Nacionalni plan|Plan)[\\p{L}]*", FLAGS);
    private static final Pattern NAME_BREAK = Pattern.compile("[,;:()\\n]|(?<!\\d)\\.");
    private static final Pattern SUBJECT_START = Pattern.compile("^\\s*(?:\\S+\\s+)??o\\s+", FLAGS);
    private static final Pattern QUOTES_AND_SPACES = Pattern.compile("[„“\"»«”'(,]+|\\s+");

    private GazetteCitationParser() {
    }

    public static List<Citation> parse(String text) {
        Set<Citation> citations = new LinkedHashSet<>();
        if (text == null) {
            return List.of();
        }
        Matcher marker = MARKER.matcher(text);
        while (marker.find() && citations.size() < MAX_CITATIONS) {
            List<String> issues = issuesAfter(text, marker.end());
            if (issues.isEmpty()) {
                continue;
            }
            String citedTitle = titleBefore(text, marker.start());
            issues.forEach(issue -> citations.add(new Citation(citedTitle, issue)));
        }
        return List.copyOf(citations);
    }

    public static boolean refersTo(String citedTitle, String regulationTitle) {
        String subject = subjectOf(citedTitle);
        return subject != null && regulationTitle != null
                && (" " + normalize(regulationTitle) + " ").contains(" " + subject);
    }

    public static String issueUrl(String gazetteNumber) {
        String[] parts = gazetteNumber.split("/");
        return ISSUE_URL.formatted(parts[1], parts[0]);
    }

    static String subjectOf(String citedTitle) {
        if (citedTitle == null) {
            return null;
        }
        Matcher act = ACT.matcher(citedTitle);
        if (!act.lookingAt()) {
            return null;
        }
        String rest = citedTitle.substring(act.end());
        String subject = normalize(SUBJECT_START.matcher(rest).replaceFirst(""));
        if (subject.length() < MIN_SUBJECT_CHARS) {
            return null;
        }
        return subject.substring(0, Math.min(SUBJECT_CHARS, subject.length())).strip();
    }

    private static List<String> issuesAfter(String text, int from) {
        String window = text.substring(from, Math.min(text.length(), from + NUMBERS_WINDOW_CHARS));
        Matcher end = WINDOW_END.matcher(window);
        if (end.find()) {
            window = window.substring(0, end.start());
        }
        List<String> issues = new ArrayList<>();
        Matcher issue = ISSUE.matcher(window);
        while (issue.find()) {
            issues.add(Integer.parseInt(issue.group(1)) + "/" + fullYear(issue.group(2)));
        }
        return issues;
    }

    private static String titleBefore(String text, int markerStart) {
        int windowStart = Math.max(0, markerStart - TITLE_WINDOW_CHARS);
        String window = text.substring(windowStart, markerStart);
        String trimmed = window.replaceAll("[\\s(„“\"»,]+$", "");
        Matcher act = ACT.matcher(trimmed);
        int titleStart = -1;
        while (act.find()) {
            if (!NAME_BREAK.matcher(trimmed.substring(act.start())).find()) {
                titleStart = act.start();
                break;
            }
        }
        if (titleStart < 0) {
            return null;
        }
        String title = trimmed.substring(titleStart).replaceAll("\\s+", " ").strip();
        return title.length() > MAX_TITLE_CHARS ? title.substring(0, MAX_TITLE_CHARS) : title;
    }

    private static int fullYear(String year) {
        if (year.length() == 4) {
            return Integer.parseInt(year);
        }
        int shortYear = Integer.parseInt(year);
        int nextShortYear = (Year.now().getValue() + 1) % 100;
        return shortYear <= nextShortYear ? 2000 + shortYear : 1900 + shortYear;
    }

    private static String normalize(String value) {
        return QUOTES_AND_SPACES.matcher(value.toLowerCase(Locale.ROOT)).replaceAll(" ").strip();
    }
}
