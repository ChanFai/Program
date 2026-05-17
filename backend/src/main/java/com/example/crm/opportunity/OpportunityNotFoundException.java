package com.example.crm.opportunity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OpportunityNotFoundException extends RuntimeException {
    public OpportunityNotFoundException(Long id) {
        super("Opportunity not found: " + id);
    }
}
