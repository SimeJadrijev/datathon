package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import tools.jackson.databind.JsonNode;

public final class EuFundsCohesionParser {

    static final String API = "https://ekohezija.gov.hr/MISPublicApi/poziv/browse/";
    static final String PUBLIC_URL = "https://eufondovi.gov.hr/poziv/?id=";

    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");
    private static final Set<String> COMPETITIVE_PROCEDURES = Set.of("Otvoreni poziv", "Ograničeni poziv");
    private static final Map<String, FundingCallStatus> STATUSES = Map.of(
            "Otvoren", FundingCallStatus.OPEN,
            "Najavljen", FundingCallStatus.ANNOUNCED,
            "Završen", FundingCallStatus.CLOSED,
            "Zatvoren", FundingCallStatus.CLOSED);
    private static final Map<String, Set<TargetGroup>> GROUPS_BY_APPLICANT = Map.of(
            "Jedinice lokalne samouprave", Set.of(TargetGroup.LOCAL_GOVERNMENT),
            "Jedinice područne (regionalne) samouprave", Set.of(TargetGroup.LOCAL_GOVERNMENT),
            "Mikro poduzetnici", Set.of(TargetGroup.COMPANY, TargetGroup.CRAFT_BUSINESS),
            "Mali poduzetnici", Set.of(TargetGroup.COMPANY, TargetGroup.CRAFT_BUSINESS),
            "Srednji poduzetnici", Set.of(TargetGroup.COMPANY),
            "Veliki poduzetnici", Set.of(TargetGroup.COMPANY),
            "Klasteri", Set.of(TargetGroup.COMPANY),
            "Zadruge", Set.of(TargetGroup.COMPANY),
            "Organizacije civilnog društva", Set.of(TargetGroup.ASSOCIATION));

    private EuFundsCohesionParser() {
    }

    static String pageUrl(int skip, int pageSize) {
        return API + "?top=" + pageSize + "&skip=" + skip;
    }

    public static int totalCount(JsonNode page) {
        return page.path("TotalCount").asInt(0);
    }

    public static List<RawItem> parsePage(JsonNode page) {
        List<RawItem> items = new ArrayList<>();
        for (JsonNode call : page.path("Records")) {
            if (COMPETITIVE_PROCEDURES.contains(text(call, "VrstaPostupkaDodjeleNaziv"))) {
                items.add(toItem(call));
            }
        }
        return items;
    }

    private static RawItem toItem(JsonNode call) {
        String id = text(call, "ID");
        List<String> applicants = splitList(text(call, "Prijavitelji"));
        List<String> areas = splitList(text(call, "Podrucja"));
        return RawItem.builder()
                .externalId(id)
                .url(PUBLIC_URL + id)
                .title(text(call, "Naziv").strip())
                .provider(text(call, "NadleznoTijeloNaziv"))
                .publishedAt(localDate(text(call, "DatumObjavePoziva")))
                .deadlineHint(localDate(text(call, "RokZaPodnosenjeProjektnihPrijava")))
                .statusHint(STATUSES.get(text(call, "Status")))
                .typeHint(FundingCallType.GRANT)
                .categories(areas)
                .countiesHint(Set.of(FundingCall.ALL_COUNTIES))
                .targetGroupsHint(targetGroups(applicants))
                .totalAmountHint(firstPositive(amount(call, "UkupnaBespovratnaSredstva"), amount(call, "VrijednostPoziva")))
                .minAmountHint(amount(call, "NajnizaVrijednostPotpore"))
                .maxAmountHint(amount(call, "NajvisaVrijednostPotpore"))
                .text(body(call, applicants, areas))
                .extra(Map.of("code", text(call, "Oznaka"), "programme", text(call, "OperativniProgramNaziv")))
                .build();
    }

    private static String body(JsonNode call, List<String> applicants, List<String> areas) {
        StringJoiner body = new StringJoiner("\n\n");
        addLine(body, "Oznaka poziva: ", text(call, "Oznaka"));
        addLine(body, "Program: ", text(call, "OperativniProgramNaziv"));
        addLine(body, "Fond: ", text(call, "InvesticijskiFondNaziv"));
        addLine(body, "Prihvatljivi prijavitelji: ", String.join(", ", applicants));
        addLine(body, "Područja: ", String.join(", ", areas));
        for (String field : List.of("Predmet", "CiljPoziva", "SvrhaPoziva", "PrihvatljiviPrijavitelji", "Sazetak")) {
            addLine(body, "", text(call, field));
        }
        return body.toString();
    }

    private static void addLine(StringJoiner body, String label, String value) {
        if (!value.isBlank()) {
            body.add(label + value.strip());
        }
    }

    private static Set<TargetGroup> targetGroups(List<String> applicants) {
        Set<TargetGroup> groups = EnumSet.noneOf(TargetGroup.class);
        applicants.forEach(applicant -> groups.addAll(GROUPS_BY_APPLICANT.getOrDefault(applicant, Set.of())));
        return groups;
    }

    private static List<String> splitList(String value) {
        return Arrays.stream(value.split(","))
                .map(String::strip)
                .filter(part -> !part.isEmpty())
                .toList();
    }

    private static LocalDate localDate(String dateTime) {
        if (dateTime.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(dateTime).atZone(ZAGREB).toLocalDate();
        } catch (DateTimeParseException e) {
            return IsoDates.leading(dateTime);
        }
    }

    private static BigDecimal amount(JsonNode call, String field) {
        JsonNode value = call.path(field);
        if (!value.isNumber() || value.decimalValue().signum() <= 0) {
            return null;
        }
        return value.decimalValue();
    }

    private static BigDecimal firstPositive(BigDecimal first, BigDecimal second) {
        return first != null ? first : second;
    }

    private static String text(JsonNode call, String field) {
        JsonNode value = call.path(field);
        return value.isString() ? value.asString() : "";
    }
}
