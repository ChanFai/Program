package com.example.crm.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalCustomers,
        long openCustomers,
        BigDecimal openDealValue,
        BigDecimal wonDealValue,
        long followUpsDue,
        Map<CustomerStage, Long> stageCounts
) {
    public static DashboardResponse from(List<Customer> customers, LocalDate today) {
        EnumMap<CustomerStage, Long> stageCounts = new EnumMap<>(CustomerStage.class);
        for (CustomerStage stage : CustomerStage.values()) {
            stageCounts.put(stage, 0L);
        }

        BigDecimal openDealValue = BigDecimal.ZERO;
        BigDecimal wonDealValue = BigDecimal.ZERO;
        long openCustomers = 0;
        long followUpsDue = 0;

        for (Customer customer : customers) {
            stageCounts.compute(customer.getStage(), (stage, count) -> count == null ? 1 : count + 1);

            if (customer.getStage() == CustomerStage.WON) {
                wonDealValue = wonDealValue.add(customer.getDealValue());
            }
            if (customer.getStage() != CustomerStage.WON && customer.getStage() != CustomerStage.LOST) {
                openCustomers++;
                openDealValue = openDealValue.add(customer.getDealValue());
                if (customer.getNextFollowDate() != null && !customer.getNextFollowDate().isAfter(today)) {
                    followUpsDue++;
                }
            }
        }

        return new DashboardResponse(
                customers.size(),
                openCustomers,
                openDealValue,
                wonDealValue,
                followUpsDue,
                stageCounts
        );
    }
}
