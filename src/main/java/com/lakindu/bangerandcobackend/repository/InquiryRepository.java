package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//create repository for data access layer
public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
    //class to be mapped is Inquiry
    //primary key is of type Integer

    //CrudRepository is the base interface providing Crud Operations for the Entity Class.

    //JPQL Query to select all the inquiries from the Database
    @Query("SELECT inquiry FROM Inquiry inquiry WHERE inquiry.isReplied = false AND inquiry.resolvedBy is null")
    List<Inquiry> getAllPendingInquiries();

    @Query("SELECT inquiry FROM Inquiry inquiry WHERE inquiry.inquiryId = :id")
    Inquiry getDetailedInquiry(int id);

}
