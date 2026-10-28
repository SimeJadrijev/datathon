package com.datathon.api.eligibility;

import com.datathon.api.common.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funding-calls/{id}")
@RequiredArgsConstructor
public class EligibilityController {

    private static final int CHECKS_PER_MINUTE = 10;

    private final EligibilityQuestionService questionService;
    private final EligibilityCheckService checkService;
    private final RateLimiter rateLimiter;

    @GetMapping("/eligibility-questions")
    public List<EligibilityQuestion> questions(@PathVariable Long id) {
        return questionService.findQuestions(id);
    }

    @PostMapping("/eligibility-check")
    public EligibilityCheckResponse check(@PathVariable Long id, @Valid @RequestBody EligibilityCheckRequest body,
            HttpServletRequest request) {
        rateLimiter.check("eligibility-check:" + request.getRemoteAddr(), CHECKS_PER_MINUTE, Duration.ofMinutes(1));
        return checkService.check(id, body);
    }
}
