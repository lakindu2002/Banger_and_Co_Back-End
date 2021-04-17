package com.lakindu.bangerandcobackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.util.BangerAndCoExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component //register as a bean in the BeanManager
public class JWTAuthFilter extends OncePerRequestFilter {
    //filter responsible for validating the JWT and authenticating the user and authorizing the user.
    //filter executed everytime a request comes in and is executed only once.
    private final JWTHandler theHandler;
    private final JWTConstants theConstants;
    private final UserDetailsService userDetailsService;

    public JWTAuthFilter(JWTHandler theHandler, JWTConstants theConstants, @Qualifier("userService") UserDetailsService userDetailsService) {
        this.theHandler = theHandler;
        this.theConstants = theConstants;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            //retrieve the request header
            String jwtRequest = httpServletRequest.getHeader(theConstants.getTOKEN_HEADER());
            if (jwtRequest == null || !jwtRequest.startsWith(theConstants.getTOKEN_PREFIX())) {
                //if token is null or invalid do not authenticate
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            } else {
                //if token seems to be valid
                String retrievedToken = jwtRequest.substring(theConstants.getTOKEN_PREFIX().length()); //retrieve JWT
                String username = theHandler.extractSubjectFromToken(retrievedToken);

                if (theHandler.isTokenValid(retrievedToken, username)) {
                    //if the token has not expired

                    //authenticate the user to show that subject actually exists
                    final UserDetails authenticatedDetails = userDetailsService.loadUserByUsername(username);

                    //retrieve a Spring Authenticator for the User
                    Authentication authentication = theHandler.getAuthenticationForValidToken(httpServletRequest, authenticatedDetails);
                    //set the user as an authenticated user in spring security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    SecurityContextHolder.clearContext();
                }
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }

        } catch (Exception ex) {
            //if an exception occurs, send a response back to the user.
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            BangerAndCoExceptionHandler theException = new BangerAndCoExceptionHandler(
                    "authentication failed",
                    ex.getMessage(),
                    HttpStatus.UNAUTHORIZED.value(),
                    null
            );

            //write JSON response to the client using Jackson Project
            new ObjectMapper().writer().writeValue(httpServletResponse.getOutputStream(), theException);
        }
    }
}
