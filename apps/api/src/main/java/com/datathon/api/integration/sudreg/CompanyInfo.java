package com.datathon.api.integration.sudreg;

public record CompanyInfo(String name, Seat seat, String legalForm, String activityCode) {

    public record Seat(String municipality, String county) {
    }
}
