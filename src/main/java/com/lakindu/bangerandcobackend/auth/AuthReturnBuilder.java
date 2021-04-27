package com.lakindu.bangerandcobackend.auth;

import com.lakindu.bangerandcobackend.dto.AuthReturnDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.http.HttpHeaders;

public class AuthReturnBuilder {
    //class used as a helper to communicate between Auth Success State and Controller
    private final AuthReturnDTO userDTO;
    private final HttpHeaders returnHeaders;
    private final BangerAndCoResponse theAPIResponse;

    public AuthReturnBuilder(AuthReturnDTO userDTO, HttpHeaders returnHeaders, BangerAndCoResponse theAPIResponse) {
        this.userDTO = userDTO;
        this.returnHeaders = returnHeaders;
        this.theAPIResponse = theAPIResponse;
    }

    public AuthReturnDTO getUserDTO() {
        return userDTO;
    }

    public HttpHeaders getReturnHeaders() {
        return returnHeaders;
    }

    public BangerAndCoResponse getTheAPIResponse() {
        return theAPIResponse;
    }
}
