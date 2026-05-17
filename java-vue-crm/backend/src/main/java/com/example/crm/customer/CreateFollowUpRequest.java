package com.example.crm.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateFollowUpRequest(
        @NotNull Long customerId,
        String ownerName,
        String type,
        @NotBlank String content,
        LocalDate followDate,
        LocalDate nextFollowDate
) {
}
