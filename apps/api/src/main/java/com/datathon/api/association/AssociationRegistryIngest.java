package com.datathon.api.association;

import com.datathon.api.ingest.RegulationIngest;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.source.SourceCode;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssociationRegistryIngest implements RegulationIngest {

    static final String REGISTER_URL = "https://data.gov.hr/ckan/dataset/b04f3da4-d2b6-4789-9e5f-dc4cc18ae6fe/resource/"
            + "030a7bf0-2d07-422d-851b-4f2df2ddc45d/download/data.json";

    private final PoliteHttpClient http;
    private final JsonMapper jsonMapper;
    private final AssociationStore associationStore;

    @Override
    public SourceCode code() {
        return SourceCode.ASSOCIATION_REGISTRY;
    }

    @Override
    public Outcome ingest() {
        byte[] register = http.getBytes(REGISTER_URL);
        Map<String, AssociationRecord> byOib = new LinkedHashMap<>();
        AssociationRegistryParser.parseActive(new ByteArrayInputStream(register), jsonMapper,
                record -> byOib.putIfAbsent(record.oib(), record));
        List<AssociationRecord> records = new ArrayList<>(byOib.values());
        associationStore.replaceAll(records);
        log.info("Registar udruga: {} active associations", records.size());
        return new Outcome(records.size(), 0, null);
    }
}
