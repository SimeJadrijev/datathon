package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;
import java.util.List;

public interface SourceFetcher {

    SourceCode code();

    List<RawItem> fetchList();

    RawItem fetchDetail(RawItem item);
}
