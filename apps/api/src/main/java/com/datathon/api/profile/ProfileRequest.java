package com.datathon.api.profile;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.TargetGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@HasTopic
public record ProfileRequest(
        @NotNull(message = "obavezno polje") TargetGroup type,
        List<@NotNull(message = "ne smije biti prazno") TargetGroup> additionalTypes,
        List<@NotNull(message = "ne smije biti prazno") Area> areas,
        @Size(max = 100, message = "smije imati najviše 100 znakova") String county,
        @Size(max = 10, message = "smije imati najviše 10 stavki")
        List<@NotBlank(message = "ne smije biti prazno") @Size(max = 50, message = "smije imati najviše 50 znakova") String> keywords,
        List<@Pattern(regexp = "\\d{2}(\\.\\d{1,2})?", message = "neispravan oblik šifre djelatnosti") String> activityCodes) {

    public ProfileRequest {
        additionalTypes = additionalTypes == null ? List.of() : additionalTypes;
        areas = areas == null ? List.of() : areas;
        keywords = keywords == null ? List.of() : keywords;
        activityCodes = activityCodes == null ? List.of() : activityCodes;
    }
}
