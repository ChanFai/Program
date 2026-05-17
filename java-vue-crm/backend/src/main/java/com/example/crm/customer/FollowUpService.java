package com.example.crm.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FollowUpService {
    private final CustomerService customerService;
    private final FollowUpRepository followUpRepository;

    public FollowUpService(CustomerService customerService, FollowUpRepository followUpRepository) {
        this.customerService = customerService;
        this.followUpRepository = followUpRepository;
    }

    public List<FollowUpListResponse> list(String keyword, boolean dueOnly) {
        String cleanKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return followUpRepository.search(cleanKeyword, dueOnly, LocalDate.now()).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional
    public FollowUpListResponse create(CreateFollowUpRequest request) {
        FollowUpRequest followUpRequest = new FollowUpRequest(
                request.ownerName(),
                request.type(),
                request.content(),
                request.followDate(),
                request.nextFollowDate()
        );
        CustomerDetailResponse customer = customerService.addFollowUp(request.customerId(), followUpRequest);
        FollowUpResponse saved = customer.followUps().stream()
                .filter(followUp -> followUp.content().equals(request.content()))
                .findFirst()
                .orElse(customer.followUps().get(0));
        return new FollowUpListResponse(
                saved.id(),
                customer.id(),
                customer.name(),
                customer.stage(),
                saved.ownerName(),
                saved.type(),
                saved.content(),
                saved.followDate(),
                saved.nextFollowDate()
        );
    }

    private FollowUpListResponse toListResponse(FollowUpRecord followUp) {
        Customer customer = followUp.getCustomer();
        return new FollowUpListResponse(
                followUp.getId(),
                customer.getId(),
                customer.getName(),
                customer.getStage(),
                followUp.getOwnerName(),
                followUp.getType(),
                followUp.getContent(),
                followUp.getFollowDate(),
                followUp.getNextFollowDate()
        );
    }
}
