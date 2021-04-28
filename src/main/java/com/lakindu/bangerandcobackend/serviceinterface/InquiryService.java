package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;

import java.util.List;

public interface InquiryService {
    Inquiry saveInquiry(InquiryDTO requestInquiry);

    List<InquiryDTO> getAllPendingInquiries();

    void removeInquiry(int id);
}
