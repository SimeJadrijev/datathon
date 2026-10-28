package com.datathon.api.profile;

import com.datathon.api.common.Oib;
import jakarta.validation.constraints.NotBlank;

public record OibRequest(@NotBlank(message = "obavezno polje") @Oib String oib) {
}
