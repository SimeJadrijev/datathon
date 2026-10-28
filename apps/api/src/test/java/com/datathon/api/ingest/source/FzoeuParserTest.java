package com.datathon.api.ingest.source;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.ingest.extract.PdfTextExtractor;
import com.datathon.api.ingest.extract.RuleExtractor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class FzoeuParserTest {

    @Test
    void parsesAccordionRows() throws IOException {
        List<RawItem> items = FzoeuParser.parseList(new String(fixture("list.html"), StandardCharsets.UTF_8));

        assertThat(items).hasSize(29);
        RawItem vehicles = items.stream().filter(item -> item.externalId().equals("275")).findFirst().orElseThrow();
        assertThat(vehicles.title())
                .isEqualTo("Javni poziv za sufinanciranje energetski učinkovitih vozila za fizičke osobe (EnU-2/26)");
        assertThat(vehicles.statusHint()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(vehicles.publishedAt()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(vehicles.countiesHint()).containsExactly("ALL");
        assertThat(vehicles.url()).isEqualTo("https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367#275");
        assertThat(vehicles.text()).contains("20.000.000,00 eura");
        assertThat(vehicles.attachments()).extracting(RawAttachment::url)
                .contains("https://www.fzoeu.hr/docs/275/Javni poziv EnU-2_26.pdf");
    }

    @Test
    void picksCallDocumentsBeforeForms() {
        List<RawAttachment> picked = PdfTextExtractor.pick(List.of(
                new RawAttachment("Prilog 2. Obrazac 2. Izjava", "https://www.fzoeu.hr/docs/275/Prilog 2.pdf"),
                new RawAttachment("Prilog 1. Obrazac 1. Prijavni obrazac", "https://www.fzoeu.hr/docs/275/Prilog 1.xlsx"),
                new RawAttachment("Upute za korištenje sustava", "https://www.fzoeu.hr/docs/275/Upute.pdf"),
                new RawAttachment("Javni poziv EnU-2_26", "https://www.fzoeu.hr/docs/275/Javni poziv EnU-2_26.pdf")));

        assertThat(picked).extracting(RawAttachment::name)
                .containsExactly("Upute za korištenje sustava", "Javni poziv EnU-2_26", "Prilog 2. Obrazac 2. Izjava");
    }

    @Test
    void readsTextFromCallPdf() throws IOException {
        String text = PdfTextExtractor.readText(fixture("javni-poziv-enu-2-26.pdf"));

        assertThat(text).contains("9.000,00 €").contains("31. ožujka 2028");
    }

    @Test
    void vehicleCallHasNoApplicationDeadlineFromThePurchasePeriod() throws IOException {
        RawItem listed = FzoeuParser.parseList(new String(fixture("list.html"), StandardCharsets.UTF_8)).stream()
                .filter(item -> item.externalId().equals("275")).findFirst().orElseThrow();
        RawItem detailed = listed.toBuilder()
                .pdfText(PdfTextExtractor.readText(fixture("javni-poziv-enu-2-26.pdf")))
                .build();

        ExtractedFields fields = new RuleExtractor().extract(detailed);

        assertThat(fields.deadline()).isNull();
        assertThat(fields.deadlineNote()).isEqualTo("do iskorištenja sredstava");
        assertThat(fields.totalAmount()).isEqualByComparingTo("20000000");
        assertThat(fields.status()).isEqualTo(FundingCallStatus.OPEN);
    }

    @Test
    void unreadablePdfGivesEmptyText() {
        assertThat(PdfTextExtractor.readText("not a pdf".getBytes(StandardCharsets.UTF_8))).isEmpty();
    }

    private static byte[] fixture(String name) throws IOException {
        try (InputStream input = new ClassPathResource("fixtures/fzoeu/" + name).getInputStream()) {
            return input.readAllBytes();
        }
    }
}
