package com.datathon.api.ingest.extract;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class FundingFilter {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;

    private static final Pattern NOT_FUNDING = Pattern.compile(
            "radn(o|a|i|om) mjest|prijam u (radni odnos|službu)|zapošljavanj[ea] (u|na)|oglas za prijam|"
                    + "javn[ai] nabav|istraživanj[ea] tržišta|zakup|prodaj[ua] |kupnj[ua] nekretnin|savjetovanj|"
                    + "izbor (na|člana|kandidat)|imenovanj[ea] (ravnatelj|pročelni|člana)|ocjenjivač|vanjsk(e|ih) stručnjak|"
                    + "rješenje o|obavijest o (provjeri|terminu|rezultatima testiranja)|provjer[ae] znanja|"
                    + "poništenj|obustav|prinudnog upravitelja|upravitelj[ae] zgrad|natječaj za radna mjesta|"
                    + "lektor|javnog dobra|preimenovanj|regulacij|parkiranj|davanj[ea] na korištenje|površin[ae] javne|"
                    + "prikupljanj[ea] ponuda|dozvol[ae]|pomorsk(o|om) dobr|koncesij|najam|iznajmljivanj|stanova|nekretnin|"
                    + "javn(o|a) priznanj|kreditn(e|a) ocjen|odgod[ua] plaćanja|rezultati|ravnatelj|proslavi|izabran|konzultant",
            FLAGS);

    private static final Pattern FUNDING = Pattern.compile(
            "natje[čc]aj|javn(i|og) poziv|poziv (na|za)|potpor|poticaj|sufinanc|financiranj|stipendij|"
                    + "subvencij|bespovratn|vaučer|vaucer|nagrad|zajam|\\bkredit|jamstv|mjer[ae]",
            FLAGS);

    public boolean isFunding(String title) {
        if (title == null) {
            return false;
        }
        return FUNDING.matcher(title).find() && !NOT_FUNDING.matcher(title).find();
    }
}
