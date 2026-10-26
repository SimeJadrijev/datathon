package com.datathon.api.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.source.SourceCode;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

class SeedFundingCallTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void mapsSnapshotRecordToEntity() throws IOException {
        FundingCall fundingCall = readFixture().fundingCalls().getFirst().toNewEntity();

        assertThat(fundingCall.getSource()).isEqualTo(SourceCode.FZOEU);
        assertThat(fundingCall.getExternalId()).isEqualTo("275");
        assertThat(fundingCall.getStatus()).isEqualTo(FundingCallStatus.OPEN);
        // EnU-2/26 has no application deadline (2028 is the end of the purchase period)
        assertThat(fundingCall.getDeadline()).isNull();
        assertThat(fundingCall.getMaxAmount()).isEqualByComparingTo(new BigDecimal("9000"));
        assertThat(fundingCall.getCounties()).containsExactly(FundingCall.ALL_COUNTIES);
        assertThat(fundingCall.getAreas()).containsExactlyInAnyOrder(Area.TRANSPORT, Area.ENERGY);
        assertThat(fundingCall.getAttachments()).singleElement()
                .satisfies(attachment -> assertThat(attachment.getUrl()).endsWith("Javni poziv EnU-2_26.pdf"));
        assertThat(fundingCall.getFetchedAt()).isEqualTo(Instant.parse("2026-10-24T08:15:00Z"));
    }

    private FundingCallSnapshot readFixture() throws IOException {
        try (InputStream input = new ClassPathResource("fixtures/seed/funding-calls.json").getInputStream()) {
            return jsonMapper.readValue(input, FundingCallSnapshot.class);
        }
    }
}
