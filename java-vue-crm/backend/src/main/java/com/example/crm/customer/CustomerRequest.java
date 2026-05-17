package com.example.crm.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CustomerRequest(
        @NotBlank String name,
        @NotBlank String industry,
        @NotBlank String source,
        @NotBlank String ownerName,
        CustomerStage stage,
        @DecimalMin("0.0") BigDecimal dealValue,
        String phone,
        @Email String email,
        String address,
        String remark,
        LocalDate nextFollowDate,
        @Valid List<ContactRequest> contacts
) {
}
