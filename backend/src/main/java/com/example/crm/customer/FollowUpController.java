package com.example.crm.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follow-ups")
public class FollowUpController {
    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    public List<FollowUpListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean dueOnly
    ) {
        return followUpService.list(keyword, dueOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpListResponse create(@Valid @RequestBody CreateFollowUpRequest request) {
        return followUpService.create(request);
    }
}
