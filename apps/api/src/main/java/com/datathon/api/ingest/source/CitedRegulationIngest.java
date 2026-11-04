package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.LegalReference;
import com.datathon.api.ingest.RegulationIngest;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.regulation.GazetteCitationParser;
import com.datathon.api.source.SourceCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CitedRegulationIngest implements RegulationIngest {

    static final int MAX_ISSUES = 300;
    static final String ISSUE_URL = NarodneNoviniParser.BASE_URL
            + "/search.aspx?sortiraj=4&kategorija=1&godina=%s&broj=%s&rpp=200&qtype=1&pretraga=da";

    private final PoliteHttpClient http;
    private final RegulationUpserter upserter;
    private final FundingCallRepository fundingCallRepository;
    private final Clock clock;

    @Autowired
    public CitedRegulationIngest(PoliteHttpClient http, RegulationUpserter upserter,
            FundingCallRepository fundingCallRepository) {
        this(http, upserter, fundingCallRepository, Clock.systemDefaultZone());
    }

    CitedRegulationIngest(PoliteHttpClient http, RegulationUpserter upserter, FundingCallRepository fundingCallRepository,
            Clock clock) {
        this.http = http;
        this.upserter = upserter;
        this.fundingCallRepository = fundingCallRepository;
        this.clock = clock;
    }

    @Override
    public SourceCode code() {
        return SourceCode.NARODNE_NOVINE;
    }

    @Override
    public Outcome ingest() {
        int fetched = 0;
        int errors = 0;
        String errorMessage = null;
        for (Map.Entry<String, List<String>> issue : citedTitlesByOlderIssue().entrySet()) {
            try {
                List<ParsedRegulation> cited = citedActs(issue.getKey(), issue.getValue());
                upserter.upsertAll(cited);
                fetched += cited.size();
            } catch (HttpFetchException | RobotsBlockedException e) {
                errors++;
                errorMessage = "NN " + issue.getKey() + ": " + e.getMessage();
                log.warn("Narodne novine: cited issue {} not read: {}", issue.getKey(), e.getMessage());
            }
        }
        log.info("Narodne novine: {} older regulations cited by funding calls", fetched);
        return new Outcome(fetched, errors, errorMessage);
    }

    private Map<String, List<String>> citedTitlesByOlderIssue() {
        int firstFullYear = LocalDate.now(clock).getYear() - 1;
        return fundingCallRepository.findDistinctTitledLegalReferences().stream()
                .filter(reference -> yearOf(reference.getGazetteNumber()) < firstFullYear)
                .collect(Collectors.groupingBy(LegalReference::getGazetteNumber, TreeMap::new,
                        Collectors.mapping(LegalReference::getCitedTitle, Collectors.toList())))
                .entrySet().stream()
                .limit(MAX_ISSUES)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));
    }

    private List<ParsedRegulation> citedActs(String gazetteNumber, List<String> citedTitles) {
        String[] parts = gazetteNumber.split("/");
        String html = http.getText(ISSUE_URL.formatted(parts[1], parts[0]));
        return NarodneNoviniParser.parseListPage(html).stream()
                .filter(act -> act.gazetteNumber().equals(gazetteNumber))
                .filter(act -> citedTitles.stream().anyMatch(title -> GazetteCitationParser.refersTo(title, act.title())))
                .toList();
    }

    private static int yearOf(String gazetteNumber) {
        return Integer.parseInt(gazetteNumber.substring(gazetteNumber.indexOf('/') + 1));
    }
}
