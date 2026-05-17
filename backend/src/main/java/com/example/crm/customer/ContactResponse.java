package com.example.crm.customer;

public record ContactResponse(
        Long id,
        String name,
        String title,
        String phone,
        String email,
        boolean primaryContact
) {
}
