package com.example.crm.opportunity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    @Query("""
            select o from Opportunity o
            join fetch o.customer c
            where (:stage is null or o.stage = :stage)
              and (:keyword is null
                or lower(o.name) like lower(concat('%', :keyword, '%'))
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(o.ownerName) like lower(concat('%', :keyword, '%'))
                or lower(o.nextStep) like lower(concat('%', :keyword, '%')))
            order by o.updatedAt desc nulls last, o.id desc
            """)
    List<Opportunity> search(@Param("keyword") String keyword, @Param("stage") OpportunityStage stage);
}
