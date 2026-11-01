package com.datathon.api.fundingcall;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marks open calls whose deadline has passed as closed: after the seed is loaded, after every ingest and each night.
 * Fixing the stored status keeps the list filter, the status facet and the counts on the home page in agreement.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class ExpiredFundingCallCloser implements ApplicationRunner {

    private final FundingCallRepository fundingCallRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        closeExpired();
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void closeExpired() {
        int closed = fundingCallRepository.closeExpired(LocalDate.now());
        if (closed > 0) {
            log.info("Closed {} funding calls whose deadline has passed", closed);
        }
    }
}
