package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tools.jackson.databind.JsonNode;

public final class HzzParser {

    static final String PROVIDER = "Hrvatski zavod za zapošljavanje";
    static final String CONTINUOUS = "kontinuirano, bez roka";
    static final String NOT_ACTIVE = "mjera više nije aktivna";

    private static final String ACTIVE_YEAR_TAG = "Aktivne mjere";
    private static final Map<String, Set<TargetGroup>> GROUPS_BY_CATEGORY = Map.of(
            "Poslodavac", Set.of(TargetGroup.COMPANY, TargetGroup.CRAFT_BUSINESS),
            "Nezaposlen", Set.of(TargetGroup.CITIZEN),
            "Samozapošljavanje", Set.of(TargetGroup.CITIZEN),
            "Zaposlen", Set.of(TargetGroup.CITIZEN));

    private HzzParser() {
    }

    public static Map<Integer, String> termNames(JsonNode terms) {
        Map<Integer, String> names = new LinkedHashMap<>();
        for (JsonNode term : terms) {
            names.put(term.path("id").intValue(), term.path("name").asString());
        }
        return names;
    }

    public static List<RawItem> parseMeasures(JsonNode measures, Map<Integer, String> years,
            Map<Integer, String> categories) {
        List<RawItem> items = new ArrayList<>();
        for (JsonNode measure : measures) {
            items.add(toItem(measure, years, categories));
        }
        return items;
    }

    private static RawItem toItem(JsonNode measure, Map<Integer, String> years, Map<Integer, String> categories) {
        String link = measure.path("link").asString();
        Document content = Jsoup.parse(measure.path("content").path("rendered").asString(), link);
        List<String> categoryNames = names(measure.path("measures_category"), categories);
        boolean isActive = names(measure.path("measures_years"), years).contains(ACTIVE_YEAR_TAG);
        return RawItem.builder()
                .externalId(String.valueOf(measure.path("id").intValue()))
                .url(link)
                .title(Jsoup.parse(measure.path("title").path("rendered").asString()).text().strip())
                .provider(PROVIDER)
                .publishedAt(IsoDates.leading(measure.path("date").asString("")))
                .statusHint(isActive ? FundingCallStatus.OPEN : FundingCallStatus.CLOSED)
                .deadlineNoteHint(isActive ? CONTINUOUS : NOT_ACTIVE)
                .typeHint(FundingCallType.SUBSIDY)
                .categories(categoryNames)
                .countiesHint(Set.of(FundingCall.ALL_COUNTIES))
                .targetGroupsHint(targetGroups(categoryNames))
                .text(HtmlText.lines(content.body()))
                .attachments(DocumentLinks.in(content.body()))
                .build();
    }

    private static List<String> names(JsonNode ids, Map<Integer, String> namesById) {
        List<String> names = new ArrayList<>();
        for (JsonNode id : ids) {
            String name = namesById.get(id.intValue());
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    private static Set<TargetGroup> targetGroups(List<String> categoryNames) {
        Set<TargetGroup> groups = EnumSet.noneOf(TargetGroup.class);
        categoryNames.forEach(name -> groups.addAll(GROUPS_BY_CATEGORY.getOrDefault(name, Set.of())));
        return groups;
    }
}
