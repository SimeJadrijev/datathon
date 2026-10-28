package com.datathon.api.profile;

public record OibProfileResponse(
        String companyName,
        Seat seat,
        String activityCode,
        ProfileRequest profile,
        String source) {

    static final String SUDREG_SOURCE = "sudreg";

    public record Seat(String municipality, String county) {
    }
}
