package com.datathon.api.integration.narodnenovine;

import com.datathon.api.common.ExternalSourceException;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Reads the text of an act from Narodne novine. robots.txt allows /clanci/sluzbeni/ articles, but not /clanci/oglasi/,
 * so only the official part is read. At most one request per second.
 */
@Component
@Slf4j
public class NarodneNovineClient {

    private static final String SOURCE = "narodne-novine";
    private static final String USER_AGENT = "datathon-natjecaji-agregator (open data research)";
    private static final String NN_HOST = "narodne-novine.nn.hr";
    private static final int TIMEOUT_MILLIS = 30_000;
    private static final long MIN_INTERVAL_MILLIS = 1_000;

    private long lastRequestAt;

    public String fetchText(String url) {
        if (!isOfficialArticle(url)) {
            throw new ExternalSourceException(SOURCE, "only the official part (/clanci/sluzbeni/) may be read: " + url);
        }
        waitForTurn();
        try {
            Document document = Jsoup.connect(url).userAgent(USER_AGENT).followRedirects(false).timeout(TIMEOUT_MILLIS).get();
            Element content = document.selectFirst("div.sl-content");
            if (content == null) {
                throw new ExternalSourceException(SOURCE, "no article content on " + url);
            }
            return content.text();
        } catch (IOException e) {
            throw new ExternalSourceException(SOURCE, "cannot read " + url, e);
        }
    }

    private boolean isOfficialArticle(String url) {
        try {
            URI uri = URI.create(url);
            return "https".equals(uri.getScheme()) && NN_HOST.equals(uri.getHost())
                    && uri.getPath() != null && uri.getPath().startsWith("/clanci/sluzbeni/");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private synchronized void waitForTurn() {
        long wait = lastRequestAt + MIN_INTERVAL_MILLIS - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalSourceException(SOURCE, "interrupted while waiting", e);
            }
        }
        lastRequestAt = System.currentTimeMillis();
    }
}
