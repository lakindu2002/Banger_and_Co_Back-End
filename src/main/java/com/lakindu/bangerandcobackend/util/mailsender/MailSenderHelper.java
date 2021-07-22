package com.lakindu.bangerandcobackend.util.mailsender;

import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.entity.User;

public class MailSenderHelper {
    //helper class used to maintain email transactions.
    private User userToBeInformed;
    private String subject;
    private MailTemplateType templateName;

    //THESE PROPERTIES ARE USED WHEN AN INQUIRY MAIL IS BEING SENT
    private Inquiry theInquiry;
    private String inquiryReply;

    //THESE PROPERTIES ARE USED WHEN RENTAL IS REJECTED
    private String rentalRejectionReason;

    public MailSenderHelper(User userToBeInformed, String subject, MailTemplateType templateName) {
        this.userToBeInformed = userToBeInformed;
        this.subject = subject;
        this.templateName = templateName;
    }

    public MailSenderHelper() {
    }

    public MailSenderHelper(User userToBeInformed, String subject, MailTemplateType templateName, String rentalRejectionReason) {
        this.userToBeInformed = userToBeInformed;
        this.subject = subject;
        this.templateName = templateName;
        this.rentalRejectionReason = rentalRejectionReason;
    }

    public String getRentalRejectionReason() {
        return rentalRejectionReason;
    }

    public void setRentalRejectionReason(String rentalRejectionReason) {
        this.rentalRejectionReason = rentalRejectionReason;
    }

    public void setUserToBeInformed(User userToBeInformed) {
        this.userToBeInformed = userToBeInformed;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTemplateName(MailTemplateType templateName) {
        this.templateName = templateName;
    }

    public Inquiry getTheInquiry() {
        return theInquiry;
    }

    public void setTheInquiry(Inquiry theInquiry) {
        this.theInquiry = theInquiry;
    }

    public String getInquiryReply() {
        return inquiryReply;
    }

    public void setInquiryReply(String inquiryReply) {
        this.inquiryReply = inquiryReply;
    }

    public User getUserToBeInformed() {
        return userToBeInformed;
    }

    public String getSubject() {
        return subject;
    }

    public MailTemplateType getTemplateName() {
        return templateName;
    }
}
