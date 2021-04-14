package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank(message = "Email Address Cannot Be Empty")
    @Email(message = "Email Address is Poorly Formatted")
    private String emailAddress;

    @NotBlank(message = "Password Cannot Be Empty")
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
