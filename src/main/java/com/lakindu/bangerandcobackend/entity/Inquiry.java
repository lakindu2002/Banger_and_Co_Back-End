package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "inquiry")
public class Inquiry {
    //definition of entity class for Inquiry with Table Linking for ORM
    //added bean validations to this class as well via Java EE Bean validation
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //enable auto incrementing by using Databases identity generator
    @Column(name = "inquiry_id")
    private int inquiryId;

    @NotBlank(message = "Please Provide a Valid First Name")
    @Column(nullable = false, name = "first_name")
    private String firstName;

    @NotBlank(message = "Please Prove a Valid Last Name")
    @Column(nullable = false, name = "last_name")
    private String lastName;

    @NotBlank(message = "Please Provide a Valid Subject")
    @Size(min = 1, max = 1000, message = "Please Keep The Subject Between 1 to 1000 Characters")
    @Column(nullable = false, name = "inquiry_subject", length = 1000)
    private String inquirySubject;

    @NotBlank(message = "Please Provide a Valid Message")
    @Size(min = 1, max = 1000, message = "Please Keep The Message Between 1 to 1000 Characters")
    @Column(nullable = false, name = "message", length = 1000)
    private String message;

    @NotBlank(message = "Please Email Address Cannot Be Left Empty")
    @Email(message = "Please Provide a Valid Email Address")
    @Column(nullable = false, name = "email_address")
    private String emailAddress;

    @NotBlank(message = "Please Provide a Valid Contact Number")
    @Size(min = 1, max = 20, message = "Please Keep Your Contact Number Between 1 and 20 Characters")
    @Column(nullable = false, name = "contact_number", length = 20)
    private String contactNumber;

    @Column(nullable = false, name = "is_replied")
    private boolean isReplied;

    @Column(nullable = false, name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "handled_by")
    private User resolvedBy;

    public Inquiry() {
    }

    public void calculateLodgedTime() {
        //method that is used to convert the current date into a timestamp
        this.createdAt = new Timestamp(new Date().getTime());
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

    @Override
    public String toString() {
        return "Inquiry{" +
                "inquiryId=" + inquiryId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", inquirySubject='" + inquirySubject + '\'' +
                ", message='" + message + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", isReplied=" + isReplied +
                ", createdAt=" + createdAt +
                ", resolvedBy=" + resolvedBy +
                '}';
    }
}
