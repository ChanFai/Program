package com.example.crm.opportunity;

import java.math.BigDecimal;
import java.util.Map;

public record OpportunitySummaryResponse(
        long totalOpportunities,
        long openOpportunities,
        BigDecimal openAmount,
        BigDecimal weightedAmount,
        BigDecimal wonAmount,
        Map<OpportunityStage, Long> stageCounts
) {
}
