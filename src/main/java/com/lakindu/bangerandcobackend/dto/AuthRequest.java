package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class AuthRequest {
    @Email(message = "This email address is poorly formatted")
    @NotBlank(message = "Please provide a valid Email Address")
    private String emailAddress;

    @NotBlank(message = "Please provide a valid Password")
    private String password;

    public AuthRequest() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
