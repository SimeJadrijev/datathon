package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import java.net.URI;
import java.util.Set;

public record LocalSite(String url, String provider, String county) {

    private static final int MAX_ID_CHARS = 120;

    String host() {
        return URI.create(url).getHost().replaceFirst("^www\\.", "");
    }

    RawItem.RawItemBuilder item(String id) {
        String shortId = id.length() > MAX_ID_CHARS ? Integer.toHexString(id.hashCode()) + "-" + id.length() : id;
        return RawItem.builder()
                .externalId(host() + ":" + shortId)
                .provider(provider)
                .countiesHint(Set.of(county));
    }
}
