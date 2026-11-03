package com.datathon.api.fundingcall;

import java.util.List;
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

    private final FundingCallRepository fundingCallRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int dated = fundingCallRepository.fillMissingFirstSeenAt();
        if (dated > 0) {
            log.info("Set first seen time for {} funding calls", dated);
        }
        fillLegalReferences();
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
