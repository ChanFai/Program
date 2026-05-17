package com.example.crm.customer;

import java.time.LocalDate;

public record FollowUpListResponse(
        Long id,
        Long customerId,
        String customerName,
        CustomerStage customerStage,
        String ownerName,
        String type,
        String content,
        LocalDate followDate,
        LocalDate nextFollowDate
) {
}
