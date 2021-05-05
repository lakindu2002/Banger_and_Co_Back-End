package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class InquiryReplyDTO {
    @NotBlank(message = "Please Provide a Valid Inquiry ID")
    @Pattern(regexp = "^[0-9]+$", message = "Inquiry ID was not in a Valid Format (Requires Numerics)")
    private String inquiryId;

    @NotBlank(message = "Please Provide a Valid Inquiry Reply")
    private String inquiryReply;

    public InquiryReplyDTO() {
    }

    public String getInquiryId() {
        return inquiryId;
    }

    public void setInquiryId(String inquiryId) {
        this.inquiryId = inquiryId;
    }

    public String getInquiryReply() {
        return inquiryReply;
    }

    public void setInquiryReply(String inquiryReply) {
        this.inquiryReply = inquiryReply;
    }
}
