package com.datathon.api.fundingcall;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FundingCallStatuses {

    private static final Pattern CANCELLED = Pattern.compile("poništen|obustav|stavljen\\w* izvan snage",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern YEAR = Pattern.compile("(?<!\\d)(20\\d{2})(?!\\d)");

    private FundingCallStatuses() {
    }

    public static boolean isClosedByTitle(String title, LocalDate deadline, LocalDate today) {
        return isCancelled(title) || isForPastYear(title, deadline, today);
    }

    public static boolean isCancelled(String title) {
        return title != null && CANCELLED.matcher(title).find();
    }

    public static boolean isForPastYear(String title, LocalDate deadline, LocalDate today) {
        if (title == null || deadline != null) {
            return false;
        }
        int latestYear = 0;
        Matcher matcher = YEAR.matcher(title);
        while (matcher.find()) {
            latestYear = Math.max(latestYear, Integer.parseInt(matcher.group(1)));
        }
        return latestYear > 0 && latestYear < today.getYear();
    }

    public static FundingCallStatus fromDeadline(LocalDate deadline, FundingCallStatus current, LocalDate today) {
        if (deadline.isBefore(today)) {
            return FundingCallStatus.CLOSED;
        }
        return current == FundingCallStatus.ANNOUNCED ? FundingCallStatus.ANNOUNCED : FundingCallStatus.OPEN;
    }
}
