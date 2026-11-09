package com.datathon.api.ai;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.integration.llm.ResponseSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class FundingCallExtractionSchema {

    static final String PURPOSE = "extract-funding-call";

    static final String SYSTEM_PROMPT = """
            From the text of a Croatian funding call (grant, subsidy, public call, scholarship) extract structured data.
            Rules:
            - Use only what is written in the text. If a value is not there, return null or an empty list. Do not guess.
            - Amounts are in euros as a number (20.000.000,00 eura -> 20000000). Ignore amounts in any other currency (kuna, CHF, USD): return null for them.
            - totalAmount is the whole budget of the call.
            - maxAmount is the highest grant (support) amount one applicant or project can receive, or the highest flat grant per unit (per vehicle, per month, per person) if the text has a table of amounts. Never a salary, a price of an item, or an eligible cost.
            - minAmount is the lowest such grant amount, only if the text states it (for example the smallest value in a table of flat amounts); never return 0.
            - deadline is the application deadline as YYYY-MM-DD. If the text gives a closing date and also says the call closes earlier when the funds run out ("do 30.11.2026., odnosno do iskorištenja sredstava"), return that closing date. null only if there is no closing date at all (only "until funds run out"), or the date is only the end of a project, implementation or procurement period.
            - targetGroups (who may apply): CITIZEN = individuals and households, CRAFT_BUSINESS = sole traders (obrt), COMPANY = companies and entrepreneurs, ASSOCIATION = associations and other civil society organisations, LOCAL_GOVERNMENT = local and regional governments and public institutions, STUDENT = pupils and students, FARMER = family farms and agricultural holdings.
            - targetGroups: only groups the text names as applicants. If the text does not say, return an empty list. Never assume.
            - areas: the 3 most important areas at most, only areas the call is really about.
            - activityCodes: NKD activity codes only if they are explicitly listed (for example "62.01").
            - counties: Croatian counties the call applies to. ["ALL"] only if the text says the call is national or open to applicants from the whole country. If the text does not say where applicants must be from, return an empty list.
            - conditionsSummary: 1-3 sentences in Croatian: who can apply and for what. State only facts from the text; if something is not in the text, leave it out instead of saying it is unclear.
            - requiredDocuments: short names of documents the applicant attaches, at most 10.""";

    static final ResponseSchema SCHEMA = ResponseSchema.builder()
            .property("provider", nullable("string"))
            .property("type", enumOrNull(names(FundingCallType.values())))
            .property("publishedAt", nullable("string"))
            .property("deadline", nullable("string"))
            .property("totalAmount", nullable("number"))
            .property("minAmount", nullable("number"))
            .property("maxAmount", nullable("number"))
            .property("targetGroups", enumArray(names(TargetGroup.values())))
            .property("areas", enumArray(names(Area.values())))
            .property("activityCodes", stringArray())
            .property("counties", stringArray())
            .property("conditionsSummary", nullable("string"))
            .property("requiredDocuments", stringArray())
            .requiredField("provider")
            .requiredField("type")
            .requiredField("publishedAt")
            .requiredField("deadline")
            .requiredField("totalAmount")
            .requiredField("minAmount")
            .requiredField("maxAmount")
            .requiredField("targetGroups")
            .requiredField("areas")
            .requiredField("activityCodes")
            .requiredField("counties")
            .requiredField("conditionsSummary")
            .requiredField("requiredDocuments")
            .build();

    private FundingCallExtractionSchema() {
    }

    private static List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }

    private static Map<String, Object> nullable(String type) {
        return Map.of("type", List.of(type, "null"));
    }

    private static Map<String, Object> enumOrNull(List<String> values) {
        return Map.of("anyOf", List.of(Map.of("type", "string", "enum", values), Map.of("type", "null")));
    }

    private static Map<String, Object> enumArray(List<String> values) {
        return Map.of("type", "array", "items", Map.of("type", "string", "enum", values));
    }

    private static Map<String, Object> stringArray() {
        return Map.of("type", "array", "items", Map.of("type", "string"));
    }
}
