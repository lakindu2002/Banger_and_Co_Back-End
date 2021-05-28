package com.lakindu.bangerandcobackend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class used when constructing the JWT Token
 *
 * @author Lakindu Hewawasam
 */
@Component
public class JWTConstants {
    //class setup to hold the constants required by JWT

    @Value("${custom.jwt.expiration_time}")
    private long EXPIRATION_TIME;   //30 minutes

    @Value("${custom.jwt.token_issuer}")
    private String TOKEN_ISSUER;    //identifies the issuer of token

    @Value("${custom.jwt.token_header}")
    private String TOKEN_HEADER;    //key of token

    private final String TOKEN_PREFIX = "Bearer ";    //Bearer to denote the ownership of token

    @Value("${custom.jwt.authority}")
    private String AUTHORITIES;     //role of the user

    public long getEXPIRATION_TIME() {
        return EXPIRATION_TIME;
    }

    public String getTOKEN_ISSUER() {
        return TOKEN_ISSUER;
    }

    public String getTOKEN_HEADER() {
        return TOKEN_HEADER;
    }

    public String getTOKEN_PREFIX() {
        return TOKEN_PREFIX;
    }

    public String getAUTHORITIES() {
        return AUTHORITIES;
    }
}
