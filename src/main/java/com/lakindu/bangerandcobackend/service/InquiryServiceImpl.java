package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.repository.InquiryRepository;
import com.lakindu.bangerandcobackend.serviceinterface.InquiryService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
//create service layer class for cross interaction of services
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    //autowire the inquiry repository for dependency injection
    //autowire the inquiryRepository bean
    public InquiryServiceImpl(@Autowired @Qualifier("inquiryRepository") InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @Override
    public Inquiry saveInquiry(InquiryDTO requestInquiry) {
        Inquiry theSubmittingInquiry = new Inquiry();
        //construct the entity object to be persisted onto the database via JPA
        theSubmittingInquiry.calculateLodgedTime();
        theSubmittingInquiry.setReplied(false);
        theSubmittingInquiry.setFirstName(requestInquiry.getFirstName());
        theSubmittingInquiry.setLastName(requestInquiry.getLastName());
        theSubmittingInquiry.setInquirySubject(requestInquiry.getInquirySubject());
        theSubmittingInquiry.setMessage(requestInquiry.getMessage());
        theSubmittingInquiry.setContactNumber(requestInquiry.getContactNumber());
        theSubmittingInquiry.setEmailAddress(requestInquiry.getEmailAddress());

        //method to save the inquiry in the database
        //let the JAVA EE - Java Persistence API handle the underlying SQL
        return inquiryRepository.save(theSubmittingInquiry);
    }

    @Override
    public List<InquiryDTO> getAllPendingInquiries() {
        //service layer method used to get inquiries that are pending
        final List<Inquiry> inquiryList = inquiryRepository.getAllPendingInquiries();
        return getTheReturnConstructed(inquiryList);
    }

    @Override
    public void removeInquiry(int id) {
        if (inquiryRepository.existsById(id)) {
            inquiryRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("The inquiry that you requested to be removed could not be found");
        }
    }

    private List<InquiryDTO> getTheReturnConstructed(List<Inquiry> inquiryList) {
        //for the array of entity type returned, convert in to an array of DTO that can be returned to the client
        List<InquiryDTO> returningList = new ArrayList<>();
        for (Inquiry theInquiry : inquiryList) {
            InquiryDTO theDTO = new InquiryDTO();

            theDTO.setInquiryId(theInquiry.getInquiryId());
            theDTO.setFirstName(theInquiry.getFirstName());
            theDTO.setLastName(theInquiry.getLastName());
            theDTO.setInquirySubject(theInquiry.getInquirySubject());
            theDTO.setMessage(theInquiry.getMessage());
            theDTO.setEmailAddress(theInquiry.getEmailAddress());
            theDTO.setContactNumber(theInquiry.getContactNumber());
            theDTO.setReplied(theInquiry.isReplied());
            theDTO.setCreatedAt(new Date(theInquiry.getCreatedAt().getTime()).toString());
            theDTO.setResolvedByUsername(null);

            returningList.add(theDTO);
        }

        return returningList;
    }
}
