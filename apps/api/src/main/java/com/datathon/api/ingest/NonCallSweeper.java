package com.datathon.api.ingest;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.source.SourceCode;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class NonCallSweeper implements ApplicationRunner {

    static final String HIDDEN_REASON = "Nije natječaj (filtar naslova)";

    private static final Map<SourceCode, BiPredicate<CallTitleFilter, String>> FILTERS = Map.of(
            SourceCode.APPRRR, CallTitleFilter::isCall,
            SourceCode.MZOM, CallTitleFilter::isCall,
            SourceCode.LOCAL_GOVERNMENT, CallTitleFilter::isCall,
            SourceCode.AGENCIES, CallTitleFilter::isCall,
            SourceCode.GOV_HR_NEWS, CallTitleFilter::isCallInNews,
            SourceCode.UNIVERSITIES, CallTitleFilter::isStudentCall);

    private final FundingCallRepository fundingCallRepository;
    private final CallTitleFilter callTitleFilter;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        hideNonCalls();
    }

    @Transactional
    public void hideNonCalls() {
        List<FundingCall> nonCalls = fundingCallRepository.findByHiddenFalseAndSourceIn(FILTERS.keySet()).stream()
                .filter(call -> !FILTERS.get(call.getSource()).test(callTitleFilter, call.getTitle()))
                .toList();
        for (FundingCall call : nonCalls) {
            call.setHidden(true);
            call.setHiddenReason(HIDDEN_REASON);
        }
        if (!nonCalls.isEmpty()) {
            log.info("Hid {} stored posts that the current title filter does not take for funding calls", nonCalls.size());
        }
    }
}
