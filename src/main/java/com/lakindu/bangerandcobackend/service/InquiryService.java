package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.repository.InquiryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//create service layer class for cross interaction of services
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    //autowire the inquiry repository for dependency injection
    //autowire the inquiryRepository bean
    public InquiryService(@Autowired @Qualifier("inquiryRepository") InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    public Inquiry saveInquiry(Inquiry inquiryToBeSaved){
        //method to save the inquiry in the database
        //let the JAVA EE - Java Persistence API handle the underlying SQL
        return inquiryRepository.save(inquiryToBeSaved);
    }
}
