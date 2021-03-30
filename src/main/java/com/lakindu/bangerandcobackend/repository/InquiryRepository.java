package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Inquiry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
//create repository for data access layer
public interface InquiryRepository extends CrudRepository<Inquiry, Integer> {
    //class to be mapped is Inquiry
    //primary key is of type Integer

    //CrudRepository is the base interface providing Crud Operations for the Entity Class.
}
