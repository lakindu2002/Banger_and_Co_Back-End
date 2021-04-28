package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;

public interface InquiryService {
    Inquiry saveInquiry(InquiryDTO requestInquiry);
}
