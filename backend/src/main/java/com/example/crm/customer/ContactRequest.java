package com.example.crm.customer;

import jakarta.validation.constraints.Email;

public record ContactRequest(
        String name,
        String title,
        String phone,
        @Email String email,
        boolean primaryContact
) {
}
