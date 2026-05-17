package com.example.crm.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerDetailResponse(
        Long id,
        String name,
        String industry,
        String source,
        String ownerName,
        CustomerStage stage,
        BigDecimal dealValue,
        String phone,
        String email,
        String address,
        String remark,
        LocalDate nextFollowDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ContactResponse> contacts,
        List<FollowUpResponse> followUps
) {
}
