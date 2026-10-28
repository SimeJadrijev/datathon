package com.datathon.api.regulation.dto;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.RegulationType;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record RegulationFilter(
        @Size(max = 200, message = "smije imati najviše 200 znakova") String q,
        RegulationType type,
        Area area,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedTo,
        Integer page,
        Integer size) {
}
