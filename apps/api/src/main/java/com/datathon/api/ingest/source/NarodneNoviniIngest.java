package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RegulationIngest;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.ingest.source.NarodneNoviniParser.EliMetadata;
import com.datathon.api.regulation.RegulationType;
import com.datathon.api.source.SourceCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NarodneNoviniIngest implements RegulationIngest {

    static final int PAGE_SIZE = 200;
    static final int NEWEST_LAWS_WITH_ELI = 60;

    private static final String LIST_URL = NarodneNoviniParser.BASE_URL
            + "/search.aspx?sortiraj=4&kategorija=1&godina=%d&rpp=" + PAGE_SIZE + "&qtype=1&pretraga=da&str=%d";
    private static final int MAX_PAGES_PER_YEAR = 30;

    private final PoliteHttpClient http;
    private final RegulationUpserter upserter;
    private final Clock clock;

    @Autowired
    public NarodneNoviniIngest(PoliteHttpClient http, RegulationUpserter upserter) {
        this(http, upserter, Clock.systemDefaultZone());
    }

    NarodneNoviniIngest(PoliteHttpClient http, RegulationUpserter upserter, Clock clock) {
        this.http = http;
        this.upserter = upserter;
        this.clock = clock;
    }

    @Override
    public SourceCode code() {
        return SourceCode.NARODNE_NOVINE;
    }

    @Override
    public Outcome ingest() {
        int currentYear = LocalDate.now(clock).getYear();
        List<ParsedRegulation> all = new ArrayList<>();
        int errors = 0;
        String errorMessage = null;
        for (int year : new int[] {currentYear, currentYear - 1}) {
            try {
                fetchYear(year, all);
            } catch (RuntimeException e) {
                errors++;
                errorMessage = "NN " + year + ": " + e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error("Narodne novine {}: list failed", year, e);
            }
        }
        addEliMetadataToNewestLaws(all);
        log.info("Narodne novine: {} regulations, {} laws", all.size(),
                all.stream().filter(regulation -> regulation.type() == RegulationType.LAW).count());
        return new Outcome(all.size(), errors, errorMessage);
    }

    private void fetchYear(int year, List<ParsedRegulation> collected) {
        Set<String> seen = new HashSet<>();
        for (int page = 0; page < MAX_PAGES_PER_YEAR; page++) {
            List<ParsedRegulation> pageItems = NarodneNoviniParser.parseListPage(
                    http.getText(LIST_URL.formatted(year, page)));
            List<ParsedRegulation> fresh = pageItems.stream().filter(item -> seen.add(item.externalId())).toList();
            upserter.upsertAll(fresh);
            collected.addAll(fresh);
            if (fresh.isEmpty() || pageItems.size() < PAGE_SIZE) {
                break;
            }
        }
    }

    private void addEliMetadataToNewestLaws(List<ParsedRegulation> all) {
        Comparator<ParsedRegulation> newestFirst = Comparator
                .comparing(ParsedRegulation::publishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ParsedRegulation::documentNumber, Comparator.reverseOrder());
        List<ParsedRegulation> newestLaws = all.stream()
                .filter(regulation -> regulation.type() == RegulationType.LAW)
                .sorted(newestFirst)
                .limit(NEWEST_LAWS_WITH_ELI)
                .toList();
        List<ParsedRegulation> enriched = new ArrayList<>();
        for (ParsedRegulation law : newestLaws) {
            readEli(law).map(eli -> withEli(law, eli)).ifPresent(enriched::add);
        }
        upserter.upsertAll(enriched);
    }

    private Optional<EliMetadata> readEli(ParsedRegulation law) {
        try {
            return NarodneNoviniParser.parseEli(http.getText(law.url()));
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("Narodne novine: ELI metadata of {} not read: {}", law.externalId(), e.getMessage());
            return Optional.empty();
        }
    }

    private static ParsedRegulation withEli(ParsedRegulation law, EliMetadata eli) {
        return law.toBuilder()
                .enactedAt(eli.enactedAt())
                .publishedAt(eli.publishedAt() != null ? eli.publishedAt() : law.publishedAt())
                .type(eli.type() != null ? eli.type() : law.type())
                .eliMetadata(true)
                .build();
    }
}
