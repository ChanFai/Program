package com.example.crm.opportunity;

import com.example.crm.customer.Customer;
import com.example.crm.customer.CustomerNotFoundException;
import com.example.crm.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpportunityService {
    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;

    public OpportunityService(OpportunityRepository opportunityRepository, CustomerRepository customerRepository) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
    }

    public List<OpportunityResponse> list(String keyword, OpportunityStage stage) {
        String cleanKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return opportunityRepository.search(cleanKeyword, stage).stream()
                .map(this::toResponse)
                .toList();
    }

    public OpportunitySummaryResponse summary() {
        List<Opportunity> opportunities = opportunityRepository.findAll();
        EnumMap<OpportunityStage, Long> stageCounts = new EnumMap<>(OpportunityStage.class);
        for (OpportunityStage stage : OpportunityStage.values()) {
            stageCounts.put(stage, 0L);
        }

        long openOpportunities = 0;
        BigDecimal openAmount = BigDecimal.ZERO;
        BigDecimal weightedAmount = BigDecimal.ZERO;
        BigDecimal wonAmount = BigDecimal.ZERO;

        for (Opportunity opportunity : opportunities) {
            stageCounts.compute(opportunity.getStage(), (stage, count) -> count == null ? 1 : count + 1);
            if (opportunity.getStage() == OpportunityStage.WON) {
                wonAmount = wonAmount.add(opportunity.getAmount());
            }
            if (opportunity.getStage() != OpportunityStage.WON && opportunity.getStage() != OpportunityStage.LOST) {
                openOpportunities++;
                openAmount = openAmount.add(opportunity.getAmount());
                weightedAmount = weightedAmount.add(weightedAmount(opportunity));
            }
        }

        return new OpportunitySummaryResponse(
                opportunities.size(),
                openOpportunities,
                openAmount,
                weightedAmount,
                wonAmount,
                stageCounts
        );
    }

    @Transactional
    public OpportunityResponse create(OpportunityRequest request) {
        Opportunity opportunity = new Opportunity();
        apply(opportunity, request);
        return toResponse(opportunityRepository.save(opportunity));
    }

    @Transactional
    public OpportunityResponse update(Long id, OpportunityRequest request) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new OpportunityNotFoundException(id));
        apply(opportunity, request);
        return toResponse(opportunityRepository.save(opportunity));
    }

    @Transactional
    public void delete(Long id) {
        if (!opportunityRepository.existsById(id)) {
            throw new OpportunityNotFoundException(id);
        }
        opportunityRepository.deleteById(id);
    }

    private void apply(Opportunity opportunity, OpportunityRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
        opportunity.setCustomer(customer);
        opportunity.setName(request.name());
        opportunity.setStage(request.stage() == null ? OpportunityStage.DISCOVERY : request.stage());
        opportunity.setAmount(request.amount() == null ? BigDecimal.ZERO : request.amount());
        opportunity.setProbability(request.probability() == null ? 20 : request.probability());
        opportunity.setOwnerName(request.ownerName());
        opportunity.setSource(request.source());
        opportunity.setExpectedCloseDate(request.expectedCloseDate());
        opportunity.setNextStep(request.nextStep());
        opportunity.setRemark(request.remark());
    }

    private OpportunityResponse toResponse(Opportunity opportunity) {
        Customer customer = opportunity.getCustomer();
        return new OpportunityResponse(
                opportunity.getId(),
                customer.getId(),
                customer.getName(),
                opportunity.getName(),
                opportunity.getStage(),
                opportunity.getAmount(),
                opportunity.getProbability(),
                weightedAmount(opportunity),
                opportunity.getOwnerName(),
                opportunity.getSource(),
                opportunity.getExpectedCloseDate(),
                opportunity.getNextStep(),
                opportunity.getRemark(),
                opportunity.getUpdatedAt()
        );
    }

    private BigDecimal weightedAmount(Opportunity opportunity) {
        return opportunity.getAmount()
                .multiply(BigDecimal.valueOf(opportunity.getProbability()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
