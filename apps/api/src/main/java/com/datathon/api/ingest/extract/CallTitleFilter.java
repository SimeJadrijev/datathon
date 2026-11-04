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
                    + "\\bministar\\b|\\bministric|tajni[kc]|čestit|konferencij|radionic|info(rmativn)?[ -]?dan|webinar|"
                    + "isplać|(?<!za )isplat|prija?m u|prijem|radn(i|og|om) odnos|radn(o|a|ih|om) mjest|zaposlenik|vježbenik|"
                    + "oglas|(?<!(sufinanciranj|subvencioniranj|financiranj)\\w{0,3} )kupnj|kupoprodaj|nekretnin|zemljišt|"
                    + "sućut|kandidatur|testiranj|provjer[ae]? znanja|"
                    + "obustav|poništ|zatvaranj|izvješ|ispravak|dražb|koncesij|dozvol|zakup|najam|prodaj|ponud|"
                    + "imenovanj|savjetovanj|mrtvozor|intervju|rješenje o|služb[eu] (na|za)|domaćic|"
                    + "dodjel\\w* lokacij|površin\\w* javne|javn\\w* površin|natje[čc]aj\\w* za zapošljav|pokojnik|"
                    + "(?<!(troškov|sufinanciranj|subvencioniranj|financiranj)\\w{0,3} )prijevoz|"
                    + "radn\\w* mjest|referent|savjetnik|suradnik|tehničar|zapovjednik|(za|–|-) pomoćnik\\w* u nastavi|član(a|ova)\\b|"
                    + "priznanj|^odluk\\w*\\s+o\\s+(?!raspisivanj)|^pravilnik|ishod|reklamn|"
                    + "objavljen\\w* (su )?odluk|dostav\\w* odluk|odluk\\w* (o ulasku|o financiranju|povjerenstv)|"
                    + "finalist|kviz|okupio|\\bupis(a|u)?\\b|popis dobitnik|poziv na razgovor|otkazan|premještaj|"
                    + "plan\\w* objave|važn\\w* obavijest|obavijest\\w* (i upute|za korisnike|korisnicima|potencijalnim)|"
                    + "u tijeku je|pitanja i odgovori|obavljanj\\w* poslova|poslovn\\w* prostor|godišnj\\w* plan|"
                    + "odluk\\w* (župan|gradonačelnik|načelnik)|projektn\\w* partnerstv",
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

    private static final Pattern STUDENT_CALL = Pattern.compile(
            "mobilnost|erasmus|smještaj|stanovanj|fellowship|scholarship|call for|studentsk\\w* (projekt|program|aktivnost)",
            FLAGS);

    private static final Pattern NOT_A_STUDENT_CALL = Pattern.compile(
            "zvanj|asistent|docent|izbor\\w* (na|u|pristupnik|studentsk)|dobitni|odluk\\w* o (izboru|odabiru|dodjeli|raspodjeli)|"
                    + "prijedlog o raspodjeli|p r i j e d l o g|bodn\\w* granic|info[ -]?dan|podsjeća|voditelj|odbor|domsk\\w* odbor|"
                    + "izvješ|pravilnik|održan|iskustv|prenijel|zaprimljen|odobren|^odluka|ostvaren\\w* pravo|domaćin",
            FLAGS);

    private final FundingFilter fundingFilter;

    public boolean isCall(String title) {
        if (title == null) {
            return false;
        }
        boolean isFunding = fundingFilter.isFunding(title) || ALSO_FUNDING.matcher(title).find();
        return isFunding && !NOT_A_CALL.matcher(title).find();
    }

    public boolean isStudentCall(String title) {
        if (title == null || NOT_A_STUDENT_CALL.matcher(title).find()) {
            return false;
        }
        return isCall(title) || STUDENT_CALL.matcher(title).find() && !NOT_A_CALL.matcher(title).find();
    }

    public boolean isCallInNews(String title) {
        return isCall(title) && CALL_WORDING.matcher(title).find() && !NOT_A_CALL_IN_NEWS.matcher(title).find();
    }
}
