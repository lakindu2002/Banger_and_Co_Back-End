package com.lakindu.bangerandcobackend.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class JWTHandler {
    //class used to define the service methods for JWT Issuing and Decoding
    private final JWTConstants theConstants;

    @Value("${custom.jwt.secret}")
    private String signingKey;

    @Autowired
    public JWTHandler(JWTConstants theConstants) {
        this.theConstants = theConstants;
    }

    //--------------------------------------------TOKEN CREATION--------------------------------------------------------

    public String generateToken(CustomUserPrincipal thePrincipal) {
        //this method will be executed after the user credentials entered are valid.
        //this method will generate and return a JWT Token to the Client.

        String[] claimsForUser = getClaimsForUser(thePrincipal);
        final String generatedToken = JWT.create()
                .withSubject(thePrincipal.getUsername())
                .withIssuer(theConstants.getTOKEN_ISSUER())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + theConstants.getEXPIRATION_TIME()))
                .withArrayClaim(theConstants.getAUTHORITIES(), claimsForUser)
                .sign(Algorithm.HMAC256(signingKey.getBytes(StandardCharsets.UTF_8)));

        return generatedToken;
    }

    private String[] getClaimsForUser(CustomUserPrincipal thePrincipal) {
        //this method will retrieve all the authorities allocated for the User
        //the authority will be the User Role defined for the User.
        List<String> authorities = new ArrayList<>();

        for (GrantedAuthority theAuthority : thePrincipal.getAuthorities()) {
            //iterate over the granted authorities assigned for the user.
            authorities.add(theAuthority.getAuthority()); //insert the name of the authority
        }

        return authorities.toArray(new String[thePrincipal.getAuthorities().size()]); //return a string array of the roles.
    }

    //----------------------------------------------TOKEN DECODING------------------------------------------------------
    //to verify a token, first create JWTVerifier and then call verify method on the JWTVerifier
    private JWTVerifier getTokenVerifier() {

        //this builds JWT Verifier with the required algorithm and signing key.
        return JWT.require(Algorithm.HMAC256(signingKey.getBytes(StandardCharsets.UTF_8)))
                .withIssuer(theConstants.getTOKEN_ISSUER())
                .build();
    }

    public boolean isTokenValid(String token, String emailAddress) {
        final boolean isExpired = isTokenExpired(token);
        final String tokenSubject = extractSubjectFromToken(token);
        final String tokenIssuerName = extractIssuerNameFromToken(token);

        //for token to be valid, DB username === token subject && token must not be expired && issuer name must be same as creation
        return tokenSubject.equals(emailAddress) && !isExpired && tokenIssuerName.equalsIgnoreCase(theConstants.getTOKEN_ISSUER());
    }

    private String extractIssuerNameFromToken(String token) {
        //method used to get issuer name from token
        return getTokenVerifier().verify(token).getIssuer();
    }

    public String extractSubjectFromToken(String token) {
        //method returns the Subject (Username) of the token
        return getTokenVerifier().verify(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        //for token to be expired, expiration time has to be less than be current time
        final DecodedJWT verifiedToken = getTokenVerifier().verify(token);
        return verifiedToken.getExpiresAt().getTime() < new Date(System.currentTimeMillis()).getTime();
    }

    public Authentication getAuthenticationForValidToken(HttpServletRequest theRequest, List<GrantedAuthority> authorityList, String emailAddress_usernameFromToken) {
        //method used to create the authentication object required to tell Spring Security that user is valid and can be authenticated
        UsernamePasswordAuthenticationToken securityToken = new UsernamePasswordAuthenticationToken(
                emailAddress_usernameFromToken,
                null,
                authorityList
        );//password left null as we know user is already authenticated when this method is executed

        securityToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(theRequest));
        //method sets the Authenticated User details to Spring Security Context
        //showing that user is authenticated for the request

        return securityToken;
    }

    public List<GrantedAuthority> getAuthorityListForToken(String token) {
        //method executed to retrieve all the roles for the User
        final DecodedJWT verifiedToken = getTokenVerifier().verify(token); //verify the token

        //retrieve the claim "authorities" assigned while creating the token
        String[] claimList = verifiedToken.getClaim(theConstants.getAUTHORITIES()).asArray(String.class);

        List<GrantedAuthority> authorityList = new ArrayList<>();

        for (String claim : claimList) {
            authorityList.add(new SimpleGrantedAuthority(claim)); //create an array list from retrieved data
        }

        return authorityList; //return formatted array list containing user role
    }

    public Long getTokenExpirationTime(String token) {
        //method used to get the expiration date & time of the token
        return getTokenVerifier().verify(token).getExpiresAt().getTime();
    }
}
