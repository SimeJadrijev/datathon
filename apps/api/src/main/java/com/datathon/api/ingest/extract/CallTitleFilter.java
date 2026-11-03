package com.datathon.api.ingest.extract;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallTitleFilter {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;

    private static final Pattern ALSO_FUNDING = Pattern.compile("program\\w* javnih potreba|dodjel\\w* (sredstava|potpor)",
            FLAGS);

    private static final Pattern NOT_A_CALL = Pattern.compile(
            "potpisan|potpisivanj|uruč|održan|predstavi|predstavlj|sastan|posjet|obiša|obilaz|rezultat|"
                    + "\\blist[ae]\\b|rang[ -]list|privremen|odluk[aeu] o (dodjeli|odabiru|financiranju)|anket|"
                    + "ministar|ministric|tajni[kc]|čestit|konferencij|radionic|info(rmativn)?[ -]?dan|webinar|"
                    + "isplać|isplat|prija?m u|prijem|radn(i|og|om) odnos|radn(o|a|ih|om) mjest|zaposlenik|vježbenik|"
                    + "oglas|kupnj|kupoprodaj|nekretnin|zemljišt|sućut|kandidatur|testiranj|provjer[ae]? znanja|"
                    + "obustav|poništ|zatvaranj|izvješ|ispravak|dražb|koncesij|dozvol|zakup|najam|prodaj|ponud|"
                    + "imenovanj|savjetovanj|mrtvozor|intervju|rješenje o|služb[eu] (na|za)|domaćic|"
                    + "dodjel\\w* lokacij|površin\\w* javne|javn\\w* površin|natje[čc]aj\\w* za zapošljav|pokojnik|prijevoz|"
                    + "radn\\w* mjest|referent|savjetnik|suradnik|tehničar|zapovjednik|pomoćnik\\w* u nastavi|član(a|ova)\\b|"
                    + "priznanj|^odluk",
            FLAGS);

    private static final Pattern CALL_WORDING = Pattern.compile(
            "javn\\w* poziv|otvoren\\w* (je |su )?(poziv|prijav|natje[čc]aj)|"
                    + "poziv\\w* (na|za) (dodjel|podnošenj|dostav|prijav|financ|sufinanc|mikro|mal\\w* i|poduzet|udrug|"
                    + "organizacij|projekt|predlaganj)|"
                    + "natje[čc]aj\\w* (za|na) (dodjel|provedb|financ|sufinanc|stipend|interven|potpor|program)|"
                    + "započel\\w* (je )?podnošenj|stipendij|bespovratn|nagradn\\w* fond",
            FLAGS);

    private static final Pattern NOT_A_CALL_IN_NEWS = Pattern.compile(
            "dobitni|dodijeljen|dodjel\\w* nagrad|proglašen|nagradn\\w* igr|zaključen|ugovoren|neuspješ|odabran|pristiglo|"
                    + "predlaganj\\w* (predsjednik|član)|članstv|nove članove|izlagač|sajm|selekcijsk|pričuvn|policijsk|upis|"
                    + "akreditir|evaluacij|najbolj|radn\\w* mjest|popun|više sile|svinjsk|suglasnost|obavijest o objavi|"
                    + "izmjen\\w* (i dopun\\w* )?(\\p{L}+ )?(poziv|natje[čc]aj)",
            FLAGS);

    private final FundingFilter fundingFilter;

    public boolean isCall(String title) {
        if (title == null) {
            return false;
        }
        boolean isFunding = fundingFilter.isFunding(title) || ALSO_FUNDING.matcher(title).find();
        return isFunding && !NOT_A_CALL.matcher(title).find();
    }

    public boolean isCallInNews(String title) {
        return isCall(title) && CALL_WORDING.matcher(title).find() && !NOT_A_CALL_IN_NEWS.matcher(title).find();
    }
}
