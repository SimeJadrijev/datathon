package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.CroatianDates;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.source.SourceCode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ZagrebFetcher implements SourceFetcher {

    static final LocalSite ZAGREB = new LocalSite("https://www.zagreb.hr", "Grad Zagreb", "Grad Zagreb");
    static final String FIRST_PAGE = "https://www.zagreb.hr/natjecaji/860?godina=aktualno";
    static final String NEXT_PAGE = "https://www.zagreb.hr/Clanak.aspx?kategorija=860&godina=aktualno&page=%d";
    static final int MAX_PAGES = 3;

    private static final Pattern ARTICLE_ID = Pattern.compile("/(\\d+)/?$");

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;

    @Override
    public SourceCode code() {
        return SourceCode.LOCAL_GOVERNMENT;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        List<RawItem> items = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            List<RawItem> pageItems = parseList(http.getText(page == 1 ? FIRST_PAGE : NEXT_PAGE.formatted(page)));
            pageItems.stream().filter(item -> callTitleFilter.isCall(item.title())).forEach(items::add);
            if (pageItems.isEmpty()) {
                break;
            }
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return LocalPages.needsDetail(item) ? LocalPages.withDetail(http.getText(item.url()), item) : item;
    }

    static List<RawItem> parseList(String html) {
        Document document = Jsoup.parse(html, ZAGREB.url());
        List<RawItem> items = new ArrayList<>();
        for (Element entry : document.select("div.listaitem")) {
            Element link = entry.selectFirst("h4 a[href]");
            if (link == null) {
                continue;
            }
            String url = link.absUrl("href");
            String title = link.text().strip();
            Element date = entry.selectFirst(".datum");
            RawItem.RawItemBuilder item = ZAGREB.item(idOf(url))
                    .url(url)
                    .title(title)
                    .publishedAt(date == null ? null : CroatianDates.findFirst(date.text()).orElse(null));
            if (DocumentLinks.isDocument(url)) {
                item.attachments(List.of(new RawAttachment(title, url)));
            }
            items.add(item.build());
        }
        return items;
    }

    private static String idOf(String url) {
        Matcher matcher = ARTICLE_ID.matcher(url);
        return matcher.find() ? matcher.group(1) : url.substring(url.lastIndexOf('/') + 1);
    }
}
