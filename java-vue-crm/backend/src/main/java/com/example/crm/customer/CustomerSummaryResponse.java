package com.example.crm.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerSummaryResponse(
        Long id,
        String name,
        String industry,
        String ownerName,
        CustomerStage stage,
        BigDecimal dealValue,
        String phone,
        String email,
        LocalDate nextFollowDate,
        String primaryContactName,
        LocalDateTime updatedAt
) {
}
