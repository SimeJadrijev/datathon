package com.datathon.api.common;

import com.datathon.api.fundingcall.FundingCall;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CountyNames {

    private static final Set<String> WHOLE_COUNTRY_NAMES = Set.of("all", "sve", "cijela hrvatska", "podrucje cijele hrvatske");
    private static final String COUNTY_WORD = "zupanija";
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final String CITY_PREFIX = "grad ";

    private CountyNames() {
    }

    public static String normalize(String county) {
        if (county == null) {
            return "";
        }
        String name = withoutDiacritics(county.toLowerCase(Locale.ROOT)).replace(COUNTY_WORD, " ")
                .replaceAll("\\s+", " ").strip();
        if (name.startsWith(CITY_PREFIX)) {
            name = name.substring(CITY_PREFIX.length()).strip();
        }
        return WHOLE_COUNTRY_NAMES.contains(name) ? FundingCall.ALL_COUNTIES : name;
    }

    public static String withoutDiacritics(String text) {
        String decomposed = Normalizer.normalize(text.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("");
    }

    public static boolean isWholeCountry(String county) {
        return FundingCall.ALL_COUNTIES.equals(normalize(county));
    }

    public static boolean areLocal(Collection<String> counties) {
        return !counties.isEmpty() && counties.stream().noneMatch(CountyNames::isWholeCountry);
    }

    public static Set<String> storedValuesMatching(String requested, Collection<String> storedValues) {
        String wanted = normalize(requested);
        return storedValues.stream()
                .filter(stored -> isWholeCountry(stored) || normalize(stored).equals(wanted))
                .collect(Collectors.toSet());
    }
}
