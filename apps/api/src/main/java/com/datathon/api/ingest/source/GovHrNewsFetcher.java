package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.ingest.source.GovHrNewsParser.Site;
import com.datathon.api.source.SourceCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GovHrNewsFetcher implements SourceFetcher {

    static final List<String> SEARCH_TERMS = List.of("poziv", "natječaj", "prijave", "stipendij", "potpor", "nagrad");
    static final int DAYS_BACK = 120;
    static final int MAX_PAGES_PER_TERM = 3;
    static final List<Site> SITES = List.of(
            new Site("https://mpudt.gov.hr", "Ministarstvo pravosuđa, uprave i digitalne transformacije"),
            new Site("https://mfin.gov.hr", "Ministarstvo financija"),
            new Site("https://mvep.gov.hr", "Ministarstvo vanjskih i europskih poslova"),
            new Site("https://mup.gov.hr", "Ministarstvo unutarnjih poslova"),
            new Site("https://mingo.gov.hr", "Ministarstvo gospodarstva"),
            new Site("https://mint.gov.hr", "Ministarstvo turizma i sporta"),
            new Site("https://mrosp.gov.hr", "Ministarstvo rada, mirovinskoga sustava, obitelji i socijalne politike"),
            new Site("https://mzozt.gov.hr", "Ministarstvo zaštite okoliša i zelene tranzicije"),
            new Site("https://mmpi.gov.hr", "Ministarstvo mora, prometa i infrastrukture"),
            new Site("https://mpgi.gov.hr", "Ministarstvo prostornoga uređenja, graditeljstva i državne imovine"),
            new Site("https://razvoj.gov.hr", "Ministarstvo regionalnoga razvoja i fondova Europske unije"),
            new Site("https://min-kulture.gov.hr", "Ministarstvo kulture i medija"),
            new Site("https://zdravlje.gov.hr", "Ministarstvo zdravstva"),
            new Site("https://branitelji.gov.hr", "Ministarstvo hrvatskih branitelja"),
            new Site("https://mdu.gov.hr", "Ministarstvo demografije i useljeništva"),
            new Site("https://hrvatiizvanrh.gov.hr", "Središnji državni ured za Hrvate izvan Republike Hrvatske"),
            new Site("https://ljudskaprava.gov.hr", "Ured za ljudska prava i prava nacionalnih manjina"),
            new Site("https://poljoprivreda.gov.hr", "Ministarstvo poljoprivrede, šumarstva i ribarstva"));

    private static final DateTimeFormatter SEARCH_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;
    private final List<Site> sites;
    private final Clock clock;

    @Autowired
    public GovHrNewsFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter) {
        this(http, callTitleFilter, SITES, Clock.systemDefaultZone());
    }

    GovHrNewsFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter, List<Site> sites, Clock clock) {
        this.http = http;
        this.callTitleFilter = callTitleFilter;
        this.sites = sites;
        this.clock = clock;
    }

    @Override
    public SourceCode code() {
        return SourceCode.GOV_HR_NEWS;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<String, RawItem> calls = new LinkedHashMap<>();
        for (Site site : sites) {
            try {
                collectCalls(site, calls);
            } catch (HttpFetchException | RobotsBlockedException e) {
                log.warn("{}: news search failed: {}", site.baseUrl(), e.getMessage());
            }
        }
        return List.copyOf(calls.values());
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return GovHrNewsParser.parseDetail(http.getText(item.url()), item);
    }

    private void collectCalls(Site site, Map<String, RawItem> calls) {
        String since = SEARCH_DATE.format(LocalDate.now(clock).minusDays(DAYS_BACK));
        for (String term : SEARCH_TERMS) {
            for (int page = 1; page <= MAX_PAGES_PER_TERM; page++) {
                String html = http.getText(searchUrl(site, term, since, page));
                GovHrNewsParser.parseSearchPage(html, site).stream()
                        .filter(item -> callTitleFilter.isCall(item.title()))
                        .forEach(item -> calls.putIfAbsent(item.externalId(), item));
                if (!GovHrNewsParser.hasNextPage(html, page)) {
                    break;
                }
            }
        }
    }

    static String searchUrl(Site site, String term, String since, int page) {
        return site.baseUrl() + GovHrNewsParser.SEARCH_PATH + "?trazi=1&tag=&datumod=" + since + "&datumdo=&pojam="
                + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&page=" + page;
    }
}
