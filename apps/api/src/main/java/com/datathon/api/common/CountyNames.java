package com.datathon.api.common;

import com.datathon.api.fundingcall.FundingCall;
import java.text.Normalizer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CountyNames {

    private static final Set<String> WHOLE_COUNTRY_NAMES = Set.of("all", "sve", "cijela hrvatska", "podrucje cijele hrvatske");
    private static final String COUNTY_WORD = "zupanija";
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final String CITY_PREFIX = "grad ";
    private static final List<String> COUNTIES = List.of("Bjelovarsko-bilogorska", "Brodsko-posavska",
            "Dubrovačko-neretvanska", "Grad Zagreb", "Istarska", "Karlovačka", "Koprivničko-križevačka",
            "Krapinsko-zagorska", "Ličko-senjska", "Međimurska", "Osječko-baranjska", "Požeško-slavonska",
            "Primorsko-goranska", "Sisačko-moslavačka", "Splitsko-dalmatinska", "Šibensko-kninska", "Varaždinska",
            "Virovitičko-podravska", "Vukovarsko-srijemska", "Zadarska", "Zagrebačka");
    private static final Map<String, String> CANONICAL_BY_KEY = COUNTIES.stream()
            .collect(Collectors.toUnmodifiableMap(CountyNames::normalize, Function.identity()));

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

    /**
     * The one spelling we store and show: sources and the model write "Istarska županija", "ISTARSKA", "Medimurska" or
     * "Područje cijele Hrvatske". Names that are not a Croatian county (e.g. "Izvan Republike Hrvatske") stay as they are.
     */
    public static String canonical(String county) {
        String key = normalize(county);
        if (FundingCall.ALL_COUNTIES.equals(key)) {
            return FundingCall.ALL_COUNTIES;
        }
        return CANONICAL_BY_KEY.getOrDefault(key, county == null ? "" : county.strip());
    }

    /** Canonical names without blanks; a whole-country entry wins over single counties. */
    public static Set<String> canonical(Collection<String> counties) {
        if (counties == null) {
            return Set.of();
        }
        Set<String> names = new LinkedHashSet<>();
        for (String county : counties) {
            String name = canonical(county);
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names.contains(FundingCall.ALL_COUNTIES) ? Set.of(FundingCall.ALL_COUNTIES) : names;
    }

    public static String withoutDiacritics(String text) {
        String decomposed = Normalizer.normalize(text.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("");
    }

    /** One of the 21 Croatian counties (any spelling); national calls are offered there, not abroad. */
    public static boolean isCounty(String name) {
        return CANONICAL_BY_KEY.containsKey(normalize(name));
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
                .filter(stored -> (isWholeCountry(stored) && isCounty(requested)) || normalize(stored).equals(wanted))
                .collect(Collectors.toSet());
    }
}
