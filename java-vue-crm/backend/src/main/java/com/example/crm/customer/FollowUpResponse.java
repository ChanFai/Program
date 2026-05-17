package com.example.crm.customer;

import java.time.LocalDate;

public record FollowUpResponse(
        Long id,
        String ownerName,
        String type,
        String content,
        LocalDate followDate,
        LocalDate nextFollowDate
) {
}
