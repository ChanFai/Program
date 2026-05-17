package com.example.crm.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("""
            select distinct c from Customer c
            left join fetch c.contacts contacts
            where (:stage is null or c.stage = :stage)
              and (:keyword is null
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(c.industry) like lower(concat('%', :keyword, '%'))
                or lower(c.ownerName) like lower(concat('%', :keyword, '%'))
                or lower(contacts.name) like lower(concat('%', :keyword, '%')))
            order by c.updatedAt desc
            """)
    List<Customer> search(@Param("keyword") String keyword, @Param("stage") CustomerStage stage);
}
