package com.lakindu.bangerandcobackend.dto;

import com.lakindu.bangerandcobackend.util.BangerAndCoResponse;
import org.springframework.http.HttpHeaders;

public class AuthReturnBuilder {
    //class used as a helper to communicate between Auth Success State and Controller
    private final UserDTO userDTO;
    private final HttpHeaders returnHeaders;
    private final BangerAndCoResponse theAPIResponse;

    public AuthReturnBuilder(UserDTO userDTO, HttpHeaders returnHeaders, BangerAndCoResponse theAPIResponse) {
        this.userDTO = userDTO;
        this.returnHeaders = returnHeaders;
        this.theAPIResponse = theAPIResponse;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public HttpHeaders getReturnHeaders() {
        return returnHeaders;
    }

    public BangerAndCoResponse getTheAPIResponse() {
        return theAPIResponse;
    }
}
