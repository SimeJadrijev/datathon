package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;
import java.util.List;

class FakeSourceFetcher implements SourceFetcher {

    private final SourceCode code;
    private final List<RawItem> items;
    private final RuntimeException listFailure;

    private FakeSourceFetcher(SourceCode code, List<RawItem> items, RuntimeException listFailure) {
        this.code = code;
        this.items = items;
        this.listFailure = listFailure;
    }

    static FakeSourceFetcher returning(SourceCode code, RawItem... items) {
        return new FakeSourceFetcher(code, List.of(items), null);
    }

    static FakeSourceFetcher failing(SourceCode code, RuntimeException failure) {
        return new FakeSourceFetcher(code, List.of(), failure);
    }

    @Override
    public SourceCode code() {
        return code;
    }

    @Override
    public List<RawItem> fetchList() {
        if (listFailure != null) {
            throw listFailure;
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return item.toBuilder().text(item.text() + "\nRok za podnošenje prijava je 30. 11. 2026.").build();
    }
}
