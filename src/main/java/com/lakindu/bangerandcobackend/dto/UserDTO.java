package com.lakindu.bangerandcobackend.dto;

import com.lakindu.bangerandcobackend.entity.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Date;

public class UserDTO {
    @Email(message = "Please Provide a Valid Email Address")
    @NotBlank(message = "Provide a Valid Email Address")
    private String emailAddress;

    @NotBlank(message = "Please Provide a Valid First Name")
    private String firstName;

    @NotBlank(message = "Please Provide a Valid Last Name")
    private String lastName;

    @NotNull(message = "Please Provide a Valid Date of Birth")
    private Date dateOfBirth;

    @NotBlank(message = "Please Provide a Valid Contact Number")
    @Size(min = 1, max = 20)
    private String contactNumber;

    private String userPassword;

    private byte[] profilePicture;

    private boolean isBlackListed;

    private String userRole;

    public UserDTO() {
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

    public static UserDTO getDTO(User theUser) {
        UserDTO returningDTO = new UserDTO();
        returningDTO.setBlackListed(theUser.isBlackListed());
        returningDTO.setContactNumber(theUser.getContactNumber());
        returningDTO.setEmailAddress(theUser.getEmailAddress());
        returningDTO.setUserRole(theUser.getUserRole().getRoleName());
        returningDTO.setFirstName(theUser.getFirstName());
        returningDTO.setLastName(theUser.getLastName());
        returningDTO.setDateOfBirth(theUser.getDateOfBirth());
        returningDTO.setProfilePicture(theUser.getProfilePicture());

        return returningDTO;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
