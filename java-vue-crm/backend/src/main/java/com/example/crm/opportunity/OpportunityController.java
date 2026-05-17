package com.example.crm.opportunity;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {
    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public List<OpportunityResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OpportunityStage stage
    ) {
        return opportunityService.list(keyword, stage);
    }

    @GetMapping("/summary")
    public OpportunitySummaryResponse summary() {
        return opportunityService.summary();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(@Valid @RequestBody OpportunityRequest request) {
        return opportunityService.create(request);
    }

    @PutMapping("/{id}")
    public OpportunityResponse update(@PathVariable Long id, @Valid @RequestBody OpportunityRequest request) {
        return opportunityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        opportunityService.delete(id);
    }
}
