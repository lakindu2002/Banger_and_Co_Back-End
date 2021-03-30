package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class InquiryDTO {
    @NotNull(message = "First Name Cannot Be Null")
    @NotBlank(message = "First Name Cannot Be Empty")
    private String firstName;

    @NotNull(message = "Last Name Cannot Be Null")
    @NotBlank(message = "Last Name Cannot Be Empty")
    private String lastName;

    @NotNull(message = "Subject Cannot Be Null")
    @NotBlank(message = "Subject Cannot Be Empty")
    private String inquirySubject;

    @NotNull(message = "Message Cannot Be Null")
    @NotBlank(message = "Message Cannot Be Empty")
    private String message;

    @NotNull(message = "Email Address Cannot Be Null")
    @NotBlank(message = "Email Address Cannot Be Empty")
    private String emailAddress;

    @NotNull(message = "Contact Number Cannot Be Null")
    @NotBlank(message = "Contact Number Cannot Be Empty")
    private String contactNumber;

    public InquiryDTO(String firstName, String lastName, String inquirySubject, String message, String emailAddress, String contactNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.inquirySubject = inquirySubject;
        this.message = message;
        this.emailAddress = emailAddress;
        this.contactNumber = contactNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getInquirySubject() {
        return inquirySubject;
    }

    public void setInquirySubject(String inquirySubject) {
        this.inquirySubject = inquirySubject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
