package com.example.crm.dashboard;

import com.example.crm.customer.CustomerService;
import com.example.crm.customer.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final CustomerService customerService;

    public DashboardController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return customerService.dashboard();
    }
}
