package com.datathon.api.fundingcall;

import com.datathon.api.common.CountyNames;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class FundingCallBackfill implements ApplicationRunner {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\u00A0]+");

    private final FundingCallRepository fundingCallRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int dated = fundingCallRepository.fillMissingFirstSeenAt();
        if (dated > 0) {
            log.info("Set first seen time for {} funding calls", dated);
        }
        int swapped = fundingCallRepository.swapReversedAmounts();
        if (swapped > 0) {
            log.info("Swapped minimum and maximum amount for {} funding calls", swapped);
        }
        fillLegalReferences();
        tidyCountiesAndTitles();
    }

    private void tidyCountiesAndTitles() {
        int tidied = 0;
        for (FundingCall fundingCall : fundingCallRepository.findAll()) {
            boolean changed = false;
            Set<String> counties = CountyNames.canonical(fundingCall.getCounties());
            if (!counties.equals(fundingCall.getCounties())) {
                fundingCall.getCounties().clear();
                fundingCall.getCounties().addAll(counties);
                changed = true;
            }
            String title = WHITESPACE.matcher(fundingCall.getTitle()).replaceAll(" ").strip();
            if (!title.equals(fundingCall.getTitle())) {
                fundingCall.setTitle(title);
                changed = true;
            }
            tidied += changed ? 1 : 0;
        }
        if (tidied > 0) {
            log.info("Tidied county names or title spacing of {} funding calls", tidied);
        }
    }

    private void fillLegalReferences() {
        List<Long> ids = fundingCallRepository.findIdsWithGazetteTextAndNoLegalReferences();
        int filled = 0;
        for (FundingCall fundingCall : fundingCallRepository.findAllById(ids)) {
            List<LegalReference> references = LegalReference.fromText(fundingCall.getSourceText());
            if (!references.isEmpty()) {
                fundingCall.getLegalReferences().addAll(references);
                filled++;
            }
        }
        if (filled > 0) {
            log.info("Read legal references for {} funding calls", filled);
        }
    }
}
