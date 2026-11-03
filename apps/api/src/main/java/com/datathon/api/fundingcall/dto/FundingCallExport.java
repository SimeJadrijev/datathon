package com.datathon.api.fundingcall.dto;

import java.time.Instant;
import java.util.List;

public record FundingCallExport(
        String conformsTo,
        String title,
        String publisher,
        String license,
        Instant issued,
        int count,
        List<FundingCallRecord> records) {

    public static final String SCHEMA_PATH = "/api/standard/funding-call.schema.json";

    public static FundingCallExport of(List<FundingCallRecord> records, Instant issued) {
        return new FundingCallExport(
                SCHEMA_PATH,
                "Otvoreni podaci o natječajima",
                "OtvoreniRadar (prikupljeno iz javnih izvora, vidi origin uz svaki zapis)",
                "Podaci izvora pod uvjetima izvornog izdavatelja",
                issued,
                records.size(),
                records);
    }
}
