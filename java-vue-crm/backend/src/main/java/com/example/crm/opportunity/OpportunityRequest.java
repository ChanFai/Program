package com.example.crm.opportunity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpportunityRequest(
        @NotNull Long customerId,
        @NotBlank String name,
        OpportunityStage stage,
        @DecimalMin("0.0") BigDecimal amount,
        @Min(0) @Max(100) Integer probability,
        @NotBlank String ownerName,
        String source,
        LocalDate expectedCloseDate,
        String nextStep,
        String remark
) {
}
