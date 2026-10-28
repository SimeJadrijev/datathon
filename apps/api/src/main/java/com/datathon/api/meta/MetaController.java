package com.datathon.api.meta;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    @GetMapping
    MetaResponse get() {
        return metaService.get();
    }
}
