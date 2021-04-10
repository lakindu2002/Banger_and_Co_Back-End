package com.lakindu.bangerandcobackend.util.authtoken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTHandler {
    //class contains the logic for creating and validating the JWT Token.
    @Value("${custom.jwt.secret}")
    private String JWTSecret;

    @Value("${custom.jwt.issuer}")
    private String issuerName;

    public String generateJWTToken(String emailAddress) {
        Map<String, Object> theClaims = new HashMap<>();
        final String theToken = Jwts.builder()
                .setIssuer(issuerName) //set the issuer name
                .setSubject(emailAddress) //set the username of the token bearer
                .setClaims(theClaims) //assign the roles
                .setIssuedAt(new Date(System.currentTimeMillis())) //denote token created time
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) //denote token expired time
                .signWith(Keys.hmacShaKeyFor(JWTSecret.getBytes())) //sign the token with the secret key
                .compact(); //generate the token

        return theToken;
    }
}
