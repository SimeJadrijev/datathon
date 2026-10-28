package com.datathon.api.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Throws every error the handler maps, so the tests can check the one error shape without a real endpoint. */
@RestController
@RequestMapping("/test-errors")
class ErrorTestController {

    record Query(@Size(max = 5, message = "smije imati najviše 5 znakova") String q, LocalDate date) {
    }

    record Body(@NotBlank(message = "ne smije biti prazno") String name) {
    }

    @GetMapping("/not-found")
    void notFound() {
        throw new NotFoundException("Natječaj ne postoji.");
    }

    @GetMapping("/external")
    void external() {
        throw new ExternalSourceException("Sudreg", "token request failed with secret-detail");
    }

    @GetMapping("/boom")
    void boom() {
        throw new IllegalStateException("secret-detail");
    }

    @GetMapping("/invalid")
    void invalid() {
        throw new InvalidRequestException("publishedFrom: ne smije biti nakon publishedTo");
    }

    @GetMapping("/query")
    String query(@Valid Query query) {
        return "ok";
    }

    @GetMapping("/required")
    String required(@RequestParam String needed) {
        return needed;
    }

    @PostMapping("/body")
    String body(@Valid @RequestBody Body body) {
        return "ok";
    }

    @GetMapping("/page")
    PageResponse<String> page(@RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer size) {
        Pageable pageable = PageRequests.of(page, size, Sort.unsorted());
        Page<String> result = new PageImpl<>(List.of("a", "b"), pageable, 250);
        return PageResponse.from(result, String::toUpperCase);
    }
}
