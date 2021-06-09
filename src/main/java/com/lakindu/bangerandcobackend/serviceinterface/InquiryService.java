package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.dto.InquiryReplyDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface InquiryService {
    Inquiry saveInquiry(InquiryDTO requestInquiry);

    List<InquiryDTO> getAllPendingInquiries();

    void removeInquiry(int id) throws ResourceNotFoundException;

    InquiryDTO getDetailedInquiry(int id) throws ResourceNotFoundException;

    void replyToInquiry(InquiryReplyDTO replyDTO, String inquiryReply, Authentication theAuthentication) throws Exception;
}
