package com.example.crm.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUpRecord, Long> {
    @Query("""
            select f from FollowUpRecord f
            join fetch f.customer c
            where (:keyword is null
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(f.ownerName) like lower(concat('%', :keyword, '%'))
                or lower(f.type) like lower(concat('%', :keyword, '%'))
                or lower(f.content) like lower(concat('%', :keyword, '%')))
              and (:dueOnly = false or (f.nextFollowDate is not null and f.nextFollowDate <= :today))
            order by f.nextFollowDate asc nulls last, f.followDate desc nulls last, f.id desc
            """)
    List<FollowUpRecord> search(
            @Param("keyword") String keyword,
            @Param("dueOnly") boolean dueOnly,
            @Param("today") LocalDate today
    );
}
