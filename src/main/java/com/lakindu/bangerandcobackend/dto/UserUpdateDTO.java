package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserUpdateDTO {
    @NotBlank(message = "Please Provide a Valid Username")
    private String username;
    @NotBlank(message = "Please Provide a Valid Contact Number of 10 Digits")
    @Pattern(regexp = "^[0-9]+$", message = "Please provide numerical inputs for your contact number.")
    @Size(min = 10, max = 10,message = "Please Provide a Valid Contact Number of 10 Digits")
    private String contactNumber;
    private String userPassword;

    public UserUpdateDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
