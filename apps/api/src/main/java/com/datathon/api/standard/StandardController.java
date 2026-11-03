package com.datathon.api.standard;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/standard")
public class StandardController {

    static final String SCHEMA_FILE = "standard/funding-call.schema.json";

    @GetMapping("/funding-call.schema.json")
    ResponseEntity<byte[]> fundingCallSchema() {
        try (InputStream schema = new ClassPathResource(SCHEMA_FILE).getInputStream()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(schema.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
