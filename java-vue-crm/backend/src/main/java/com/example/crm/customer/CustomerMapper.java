package com.example.crm.customer;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

final class CustomerMapper {
    private CustomerMapper() {
    }

    static CustomerSummaryResponse toSummary(Customer customer) {
        String primaryContactName = customer.getContacts().stream()
                .filter(Contact::isPrimaryContact)
                .findFirst()
                .or(() -> customer.getContacts().stream().findFirst())
                .map(Contact::getName)
                .orElse(null);

        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getName(),
                customer.getIndustry(),
                customer.getOwnerName(),
                customer.getStage(),
                customer.getDealValue(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getNextFollowDate(),
                primaryContactName,
                customer.getUpdatedAt()
        );
    }

    static CustomerDetailResponse toDetail(Customer customer) {
        List<ContactResponse> contacts = customer.getContacts().stream()
                .map(CustomerMapper::toContactResponse)
                .toList();
        List<FollowUpResponse> followUps = customer.getFollowUps().stream()
                .sorted(Comparator.comparing(FollowUpRecord::getFollowDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(CustomerMapper::toFollowUpResponse)
                .toList();

        return new CustomerDetailResponse(
                customer.getId(),
                customer.getName(),
                customer.getIndustry(),
                customer.getSource(),
                customer.getOwnerName(),
                customer.getStage(),
                customer.getDealValue(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getRemark(),
                customer.getNextFollowDate(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                contacts,
                followUps
        );
    }

    static ContactResponse toContactResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getTitle(),
                contact.getPhone(),
                contact.getEmail(),
                contact.isPrimaryContact()
        );
    }

    static FollowUpResponse toFollowUpResponse(FollowUpRecord followUp) {
        return new FollowUpResponse(
                followUp.getId(),
                followUp.getOwnerName(),
                followUp.getType(),
                followUp.getContent(),
                followUp.getFollowDate(),
                followUp.getNextFollowDate()
        );
    }

    static void apply(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setIndustry(request.industry());
        customer.setSource(request.source());
        customer.setOwnerName(request.ownerName());
        customer.setStage(request.stage() == null ? CustomerStage.NEW : request.stage());
        customer.setDealValue(request.dealValue() == null ? BigDecimal.ZERO : request.dealValue());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setRemark(request.remark());
        customer.setNextFollowDate(request.nextFollowDate());

        customer.getContacts().clear();
        if (request.contacts() != null) {
            request.contacts().stream()
                    .map(contactRequest -> toContact(customer, contactRequest))
                    .forEach(customer.getContacts()::add);
        }
    }

    static Contact toContact(Customer customer, ContactRequest request) {
        Contact contact = new Contact();
        contact.setCustomer(customer);
        contact.setName(request.name());
        contact.setTitle(request.title());
        contact.setPhone(request.phone());
        contact.setEmail(request.email());
        contact.setPrimaryContact(request.primaryContact());
        return contact;
    }

    static FollowUpRecord toFollowUp(Customer customer, FollowUpRequest request) {
        FollowUpRecord followUp = new FollowUpRecord();
        followUp.setCustomer(customer);
        followUp.setOwnerName(request.ownerName());
        followUp.setType(request.type());
        followUp.setContent(request.content());
        followUp.setFollowDate(request.followDate());
        followUp.setNextFollowDate(request.nextFollowDate());
        return followUp;
    }
}
