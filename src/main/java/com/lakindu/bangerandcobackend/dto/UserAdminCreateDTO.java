package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.*;
import java.sql.Date;

public class UserAdminCreateDTO {
    @NotBlank(message = "Please Provide a Valid Username")
    @Size(min = 6, max = 15, message = "Please keep username between 6 and 15 characters")
    private String username;

    @Email(message = "Please Provide a Valid Email Address")
    @NotBlank(message = "Provide a Valid Email Address")
    @Size(max = 255, message = "Please keep email address less than 255 characters")
    private String emailAddress;

    @NotBlank(message = "Please Provide a Valid First Name")
    @Pattern(message = "Please ensure that your first name has only alphabetical characters and no numerics", regexp = "^[A-Za-z ]+")
    @Size(max = 100, message = "Please keep first name less than 100 characters")
    private String firstName;

    @NotBlank(message = "Please Provide a Valid Last Name")
    @Pattern(message = "Please ensure that your last name has only alphabetical characters and no numerics", regexp = "^[A-Za-z ]+")
    @Size(max = 100, message = "Please keep last name less than 100 characters")
    private String lastName;

    @NotNull(message = "Please Provide a Valid Date of Birth")
    private Date dateOfBirth;

    @NotBlank(message = "Please Provide a Valid Contact Number of 10 Digits")
    @Pattern(regexp = "^[0-9]+$", message = "Please provide numerical inputs for your contact number.")
    @Size(min = 10, max = 10, message = "Please Provide a Valid Contact Number of 10 Digits")
    private String contactNumber;

    @NotBlank(message = "Please Provide a Valid Password")
    @Size(min = 6, max = 15, message = "Please keep password between 6 and 15 characters")
    private String userPassword;

    private byte[] profilePicture;

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public UserAdminCreateDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}
