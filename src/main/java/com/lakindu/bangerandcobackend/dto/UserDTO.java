package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.*;
import java.sql.Date;

public class UserDTO {

    @NotBlank(message = "Please Provide a Valid Username")
    private String username;

    @Email(message = "Please Provide a Valid Email Address")
    @NotBlank(message = "Provide a Valid Email Address")
    private String emailAddress;

    @NotBlank(message = "Please Provide a Valid First Name")
    private String firstName;

    @NotBlank(message = "Please Provide a Valid Last Name")
    private String lastName;

    @NotNull(message = "Please Provide a Valid Date of Birth")
    private Date dateOfBirth;

    @NotBlank(message = "Please Provide a Valid Contact Number of 10 Digits")
    @Pattern(regexp = "^[0-9]+$", message = "Please provide numerical inputs for your contact number.")
    @Size(min = 10, max = 10, message = "Please Provide a Valid Contact Number of 10 Digits")
    private String contactNumber;

    private String userPassword;

    private byte[] profilePicture;

    private boolean isBlackListed;

    private String userRole;

    public UserDTO() {
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

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isBlackListed() {
        return isBlackListed;
    }

    public void setBlackListed(boolean blackListed) {
        isBlackListed = blackListed;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
