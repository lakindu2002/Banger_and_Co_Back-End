package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;

import java.util.List;

public interface InquiryService {
    Inquiry saveInquiry(InquiryDTO requestInquiry);

    List<InquiryDTO> getAllPendingInquiries();

    void removeInquiry(int id);

    Inquiry getDetailedInquiry(int id);

    InquiryDTO getTheReturnConstructed(Inquiry theInquiry);

    void replyToInquiry(Inquiry theInquiry, String inquiryReply);
}
