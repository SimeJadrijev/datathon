package com.datathon.api.integration.sudreg;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.common.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

public class FixtureSudregClient implements SudregClient {

    private static final String SOURCE = "Sudreg";
    private static final String FIXTURE_FOLDER = "sudreg-fixtures/";
    private static final int OIB_LENGTH = 11;

    private final JsonMapper jsonMapper;

    public FixtureSudregClient(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public CompanyInfo findByOib(String oib) {
        ClassPathResource fixture = new ClassPathResource(FIXTURE_FOLDER + oib + ".json");
        if (!isPlainOib(oib) || !fixture.exists()) {
            throw new NotFoundException("Tvrtka s ovim OIB-om nije pronađena.");
        }
        return read(fixture).toCompanyInfo();
    }

    private static boolean isPlainOib(String oib) {
        return oib.length() == OIB_LENGTH && oib.chars().allMatch(Character::isDigit);
    }

    private SudregCompany read(ClassPathResource fixture) {
        try (InputStream input = fixture.getInputStream()) {
            return jsonMapper.readValue(input, SudregCompany.class);
        } catch (IOException e) {
            throw new ExternalSourceException(SOURCE, "Sudreg fixture cannot be read");
        }
    }
}
