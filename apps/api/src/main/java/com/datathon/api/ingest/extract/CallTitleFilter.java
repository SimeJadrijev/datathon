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
                    + "dodjel\\w* lokacij|površin\\w* javne|natje[čc]aj\\w* za zapošljav|pokojnik|prijevoz",
            FLAGS);

    private final FundingFilter fundingFilter;

    public boolean isCall(String title) {
        if (title == null) {
            return false;
        }
        boolean isFunding = fundingFilter.isFunding(title) || ALSO_FUNDING.matcher(title).find();
        return isFunding && !NOT_A_CALL.matcher(title).find();
    }
}
