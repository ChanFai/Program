package com.example.crm.customer;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record FollowUpRequest(
        String ownerName,
        String type,
        @NotBlank String content,
        LocalDate followDate,
        LocalDate nextFollowDate
) {
}
