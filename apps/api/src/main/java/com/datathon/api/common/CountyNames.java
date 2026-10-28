package com.datathon.api.common;

import com.datathon.api.fundingcall.FundingCall;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class CountyNames {

    private static final Set<String> WHOLE_COUNTRY_NAMES = Set.of("all", "sve", "cijela hrvatska", "područje cijele hrvatske");
    private static final String COUNTY_WORD = "županija";
    private static final String CITY_PREFIX = "grad ";

    private CountyNames() {
    }

    public static String normalize(String county) {
        if (county == null) {
            return "";
        }
        String name = county.toLowerCase(Locale.ROOT).replace(COUNTY_WORD, " ").replaceAll("\\s+", " ").strip();
        if (name.startsWith(CITY_PREFIX)) {
            name = name.substring(CITY_PREFIX.length()).strip();
        }
        return WHOLE_COUNTRY_NAMES.contains(name) ? FundingCall.ALL_COUNTIES : name;
    }

    public static boolean isWholeCountry(String county) {
        return FundingCall.ALL_COUNTIES.equals(normalize(county));
    }

    public static Set<String> storedValuesMatching(String requested, Collection<String> storedValues) {
        String wanted = normalize(requested);
        return storedValues.stream()
                .filter(stored -> isWholeCountry(stored) || normalize(stored).equals(wanted))
                .collect(Collectors.toSet());
    }
}
