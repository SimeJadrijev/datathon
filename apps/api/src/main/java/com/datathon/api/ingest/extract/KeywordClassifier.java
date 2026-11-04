package com.datathon.api.ingest.extract;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.TargetGroup;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KeywordClassifier {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;
    private static final int BODY_CHARS = 5000;
    private static final int GROUP_BODY_HITS = 2;
    private static final int AREA_BODY_HITS = 3;

    private static final Map<TargetGroup, Pattern> GROUPS = new LinkedHashMap<>();
    private static final Map<Area, Pattern> AREAS = new LinkedHashMap<>();

    static {
        GROUPS.put(TargetGroup.STUDENT, compile("student|učenik|redovit\\w* status|akademsk\\w* godin|stipendij"));
        GROUPS.put(TargetGroup.CITIZEN, compile("fizičk\\w* osob|građan|kućanstv|obitelj\\w* kuć|nezaposlen|roditelj"));
        GROUPS.put(TargetGroup.CRAFT_BUSINESS, compile("\\bobrt|samostaln\\w* djelatnost"));
        GROUPS.put(TargetGroup.COMPANY, compile("trgovačk\\w* društv|d\\.o\\.o|poduzetni|poslodav|mikro, mal\\w* i srednj"
                + "|\\bMSP\\b|malog gospodarstva"));
        GROUPS.put(TargetGroup.ASSOCIATION, compile("udrug|organizacij\\w* civilnog|studentsk\\w* (organizacij|zbor)|neprofitn"
                + "|OCD\\b|zaklad"));
        GROUPS.put(TargetGroup.LOCAL_GOVERNMENT, compile("jedinic\\w* lokaln|\\bJLS\\b|\\bJL\\(R\\)S|općin\\w* i grad"
                + "|gradov\\w* i općin|županij\\w* (?:mogu|kao prijavitelj)|javn\\w* ustanov"));
        GROUPS.put(TargetGroup.FARMER, compile("poljoprivredn\\w* gospodarstv|\\bOPG|poljoprivrednik|ribar"
                + "|primarn\\w* poljoprivredn"));

        AREAS.put(Area.ENVIRONMENT, compile("okoliš|otpad|klimat|bioraznolik|priroda|kompost|ekološk|zelen"));
        AREAS.put(Area.ENERGY, compile("energet|obnovljiv|fotonapon|solarn|toplinsk\\w* crpk|kondenzacijsk|emisij"));
        AREAS.put(Area.TRANSPORT, compile("vozil|promet|mobilnost|e-romobil|bicikl|punionic"));
        AREAS.put(Area.AGRICULTURE, compile("poljopriv|ruraln|ribarst|stočar|sjemen|pčel|šumarst|agro"));
        AREAS.put(Area.EDUCATION, compile("obrazov(n|anj)|odgoj|edukacij|škol|student|učenik|stipendij|udžbenik|izobrazb"
                + "|osposobljavanj"));
        AREAS.put(Area.SCIENCE, compile("znanost|istraživ|znanstven|akademsk"));
        AREAS.put(Area.CULTURE, compile("kultur|umjetnost|baštin|knjižn|muzej|kazališ"));
        AREAS.put(Area.SPORT, compile("\\bsport|tjelesn\\w* aktivnost"));
        AREAS.put(Area.SOCIAL, compile("socijal|siromaš|invaliditet|beskućn|starij\\w* osob|ranjiv"));
        AREAS.put(Area.HEALTH, compile("zdravstv|zdravlj|ovisnost|bolest"));
        AREAS.put(Area.EMPLOYMENT, compile("zapošljavanj|samozapošljavanj|nezaposlen|radn\\w* mjest|pripravništv"));
        AREAS.put(Area.ENTREPRENEURSHIP, compile("poduzet|obrtni|gospodarsk|investicij|mal\\w* gospodarstv|jamstv|zajam"
                + "|kredit"));
        AREAS.put(Area.INNOVATION, compile("inovac|startup|start-up|razvoj proizvoda|pametn\\w* specijalizacij|\\bS3\\b"));
        AREAS.put(Area.DIGITALIZATION, compile("digital"));
        AREAS.put(Area.TOURISM, compile("turiz"));
        AREAS.put(Area.YOUTH, compile("\\bmlad(i|ih|ima)\\b|mladež"));
        AREAS.put(Area.CIVIL_SOCIETY, compile("udrug|civiln\\w* društv|volonter"));
    }

    private KeywordClassifier() {
    }

    public static Set<TargetGroup> targetGroups(String title, String body) {
        return classify(GROUPS, title, body, GROUP_BODY_HITS, TargetGroup.class);
    }

    public static Set<Area> areas(String title, String body) {
        return classify(AREAS, title, body, AREA_BODY_HITS, Area.class);
    }

    private static <E extends Enum<E>> Set<E> classify(Map<E, Pattern> patterns, String title, String body,
            int bodyHits, Class<E> type) {
        Set<E> found = EnumSet.noneOf(type);
        String limitedBody = body == null ? "" : body.substring(0, Math.min(body.length(), BODY_CHARS));
        for (Map.Entry<E, Pattern> entry : patterns.entrySet()) {
            if (countHits(entry.getValue(), title) >= 1 || countHits(entry.getValue(), limitedBody) >= bodyHits) {
                found.add(entry.getKey());
            }
        }
        return found;
    }

    private static int countHits(Pattern pattern, String text) {
        if (text == null) {
            return 0;
        }
        int hits = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            hits++;
        }
        return hits;
    }

    private static Pattern compile(String regex) {
        return Pattern.compile(regex, FLAGS);
    }
}
