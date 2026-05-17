package com.example.crm.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerSummaryResponse> list(String keyword, CustomerStage stage) {
        String cleanKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return customerRepository.search(cleanKeyword, stage).stream()
                .map(CustomerMapper::toSummary)
                .toList();
    }

    public CustomerDetailResponse get(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerMapper.toDetail(customer);
    }

    @Transactional
    public CustomerDetailResponse create(CustomerRequest request) {
        Customer customer = new Customer();
        CustomerMapper.apply(customer, request);
        return CustomerMapper.toDetail(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDetailResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        CustomerMapper.apply(customer, request);
        return CustomerMapper.toDetail(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public CustomerDetailResponse addFollowUp(Long id, FollowUpRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        FollowUpRecord followUp = CustomerMapper.toFollowUp(customer, request);
        customer.getFollowUps().add(followUp);
        if (request.nextFollowDate() != null) {
            customer.setNextFollowDate(request.nextFollowDate());
        }
        if (customer.getStage() == CustomerStage.NEW) {
            customer.setStage(CustomerStage.CONTACTED);
        }
        return CustomerMapper.toDetail(customerRepository.save(customer));
    }

    public DashboardResponse dashboard() {
        List<Customer> customers = customerRepository.findAll();
        return DashboardResponse.from(customers, LocalDate.now());
    }
}
