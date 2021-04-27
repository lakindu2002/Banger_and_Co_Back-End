package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdateUserDTO {
    @NotBlank(message = "Please Provide a Valid Username")
    private String username;
    @NotBlank(message = "Please Provide a Valid Contact Number")
    @Size(min = 1, max = 20, message = "Please Keep Contact Number Between 1 to 20 Characters")
    private String contactNumber;
    private String userPassword;

    public UpdateUserDTO() {
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
