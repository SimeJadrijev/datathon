package com.datathon.api.fundingcall.dto;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
class FundingCallSortConverter implements Converter<String, FundingCallSort> {

    @Override
    public FundingCallSort convert(String source) {
        return FundingCallSort.fromParameterValue(source)
                .orElseThrow(() -> new IllegalArgumentException("Unknown sort " + source));
    }
}
