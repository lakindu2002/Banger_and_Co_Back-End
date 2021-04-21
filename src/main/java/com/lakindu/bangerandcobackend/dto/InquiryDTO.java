package com.lakindu.bangerandcobackend.dto;

import com.lakindu.bangerandcobackend.entity.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

public class InquiryDTO {
    //added bean validations to this class as well via Java EE Bean validation

    private int inquiryId;

    @NotBlank(message = "Please Provide a Valid First Name")
    private String firstName;

    @NotBlank(message = "Please Prove a Valid Last Name")
    private String lastName;

    @NotBlank(message = "Please Provide a Valid Subject")
    @Size(min = 1, max = 1000, message = "Please Keep The Subject Between 1 to 1000 Characters")
    private String inquirySubject;

    @NotBlank(message = "Please Provide a Valid Message")
    @Size(min = 1, max = 1000, message = "Please Keep The Message Between 1 to 1000 Characters")
    private String message;

    @NotBlank(message = "Please Email Address Cannot Be Left Empty")
    @Email(message = "Please Provide a Valid Email Address")
    private String emailAddress;

    @NotBlank(message = "Please Provide a Valid Contact Number")
    @Size(min = 1, max = 20, message = "Please Keep Your Contact Number Between 1 and 20 Characters")
    private String contactNumber;

    private boolean isReplied;

    private Timestamp createdAt;

    private User resolvedBy;

    public InquiryDTO() {
    }

    public int getInquiryId() {
        return inquiryId;
    }

    public void setInquiryId(int inquiryId) {
        this.inquiryId = inquiryId;
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

    public boolean isReplied() {
        return isReplied;
    }

    public void setReplied(boolean replied) {
        isReplied = replied;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
}
