package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;

import java.util.List;

public interface InquiryService {
    Inquiry saveInquiry(InquiryDTO requestInquiry);

    List<InquiryDTO> getAllPendingInquiries();

    void removeInquiry(int id) throws ResourceNotFoundException;

    Inquiry getDetailedInquiry(int id) throws ResourceNotFoundException;

    InquiryDTO getTheReturnConstructed(Inquiry theInquiry);

    void replyToInquiry(Inquiry theInquiry, String inquiryReply);
}
