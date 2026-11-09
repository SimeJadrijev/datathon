package com.datathon.api.fundingcall;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Scheduled(cron = "0 5 0 * * *", zone = "Europe/Zagreb")
    @Transactional
    public void closeExpired() {
        LocalDate today = LocalDate.now();
        int closed = fundingCallRepository.closeExpired(today);
        if (closed > 0) {
            log.info("Closed {} funding calls whose deadline has passed", closed);
        }
        closeByTitle(today);
    }

    private void closeByTitle(LocalDate today) {
        List<FundingCall> outdated = fundingCallRepository.findByStatusAndDeadlineIsNull(FundingCallStatus.OPEN).stream()
                .filter(call -> FundingCallStatuses.isClosedByTitle(call.getTitle(), null, today))
                .toList();
        outdated.forEach(call -> call.setStatus(FundingCallStatus.CLOSED));
        if (!outdated.isEmpty()) {
            log.info("Closed {} open calls without a deadline that are cancelled or for a past year", outdated.size());
        }
    }
}
