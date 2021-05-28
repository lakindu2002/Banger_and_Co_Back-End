package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AuthRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 6, max = 15, message = "Please keep username between 6 and 15 characters")
    private String username;

    @NotBlank(message = "Password Cannot Be Empty")
    @Size(min = 6, max = 15, message = "Please keep password between 6 and 15 characters")
    private String password;

    public AuthRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
