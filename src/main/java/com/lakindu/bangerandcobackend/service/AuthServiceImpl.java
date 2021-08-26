package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.auth.AuthReturnBuilder;
import com.lakindu.bangerandcobackend.auth.CustomUserPrincipal;
import com.lakindu.bangerandcobackend.auth.JWTConstants;
import com.lakindu.bangerandcobackend.auth.JWTHandler;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.dto.AuthReturnDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.AuthService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {
    //service used to handle authentication
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JWTHandler theTokenIssuer;
    private final JWTConstants theConstants;

    @Autowired
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            @Qualifier("userServiceImpl") UserService userService,
            @Qualifier("JWTHandler") JWTHandler theTokenIssuer,
            @Qualifier("JWTConstants") JWTConstants theConstants
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.theTokenIssuer = theTokenIssuer;
        this.theConstants = theConstants;
    }

    @Override
    public AuthReturnBuilder performAuthentication(AuthRequest theAuthRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        theAuthRequest.getUsername(),
                        theAuthRequest.getPassword()
                ) //spring security will authenticate the user by calling the custom UserDetailsService implementation method
                //located in the UserService
        );

        User theLoggedInUser = userService._getUserWithImageDecompression(theAuthRequest.getUsername());
        CustomUserPrincipal thePrincipal = new CustomUserPrincipal(theLoggedInUser);
        String generatedToken = theTokenIssuer.generateToken(thePrincipal);

        final AuthReturnDTO convertedDTO = generateAuthReturn(theLoggedInUser);
        final HttpHeaders returningHeaders = generateHeadersForAuthSuccess(generatedToken);

        return new AuthReturnBuilder(convertedDTO, returningHeaders, new BangerAndCoResponse("authenticated successfully", HttpStatus.OK.value()));
    }

    private AuthReturnDTO generateAuthReturn(User authenticatedUser) {
        AuthReturnDTO theDTO = new AuthReturnDTO();

        theDTO.setUsername(authenticatedUser.getUsername());
        theDTO.setProfilePicture(authenticatedUser.getProfilePicture());
        theDTO.setFirstName(authenticatedUser.getFirstName());
        theDTO.setLastName(authenticatedUser.getLastName());
        theDTO.setUserRole(authenticatedUser.getUserRole().getRoleName());
        theDTO.setDateOfBirth(authenticatedUser.getDateOfBirth());
        theDTO.setBlacklisted(authenticatedUser.isBlackListed());

        return theDTO;
    }

    private HttpHeaders generateHeadersForAuthSuccess(String theToken) {
        //return the User object with the JWT Token in the response header.
        HttpHeaders theHeaders = new HttpHeaders();
        theHeaders.add("Authorization", String.format("%s%s", theConstants.getTOKEN_PREFIX(), theToken));
        theHeaders.add("Token-Expiry", String.valueOf(theTokenIssuer.getTokenExpirationTime(theToken)));
        //allow the Authorization header to be accessed in the HTTP Response.
        theHeaders.add("Access-Control-Expose-Headers", "Authorization, Token-Expiry");

        return theHeaders;
    }
}
