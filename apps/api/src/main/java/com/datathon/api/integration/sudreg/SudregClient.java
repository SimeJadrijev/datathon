package com.datathon.api.integration.sudreg;

public interface SudregClient {

    /**
     * Implementations throw {@code NotFoundException} when no company has the OIB and {@code ExternalSourceException}
     * when the register is unreachable. Neither exception may carry the OIB, because exception text is logged.
     */
    CompanyInfo findByOib(String oib);
}
