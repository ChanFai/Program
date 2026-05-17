package com.example.crm.opportunity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OpportunityResponse(
        Long id,
        Long customerId,
        String customerName,
        String name,
        OpportunityStage stage,
        BigDecimal amount,
        Integer probability,
        BigDecimal weightedAmount,
        String ownerName,
        String source,
        LocalDate expectedCloseDate,
        String nextStep,
        String remark,
        LocalDateTime updatedAt
) {
}
