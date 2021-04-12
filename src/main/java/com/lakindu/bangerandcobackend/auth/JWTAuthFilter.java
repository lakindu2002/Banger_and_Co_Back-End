package com.lakindu.bangerandcobackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.util.BangerAndCoExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component //register as a bean in the BeanManager
public class JWTAuthFilter extends OncePerRequestFilter {
    //filter responsible for validating the JWT and authenticating the user and authorizing the user.
    //filter executed everytime a request comes in and is executed only once.
    private final JWTHandler theHandler;
    private final JWTConstants theConstants;

    public JWTAuthFilter(JWTHandler theHandler, JWTConstants theConstants) {
        this.theHandler = theHandler;
        this.theConstants = theConstants;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            //request the authorization header from the request
            String requestHeader = httpServletRequest.getHeader(theConstants.getTOKEN_HEADER());
            if (requestHeader == null || !requestHeader.startsWith(theConstants.getTOKEN_PREFIX())) {
                //if request is null, or does not start with "Bearer "
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return; //block the request
            } else {
                //retrieve the JWT Token
                String passedToken = requestHeader.substring(theConstants.getTOKEN_PREFIX().length());
                String emailAddress_userName = theHandler.extractSubjectFromToken(passedToken); //retrieve subject from token

                if (theHandler.isTokenValid(passedToken, emailAddress_userName)) {
                    //retrieve authorities for the user
                    List<GrantedAuthority> authorityList = theHandler.getAuthorityListForToken(passedToken);

                    if (canGrantAuthorization(authorityList.get(0), httpServletRequest.getRequestURI())) {
                        //if the token is a valid token and user is accessing authorized endpoints.
                        //retrieve the valid authentication for the User.
                        final Authentication tokenSecurityAuthentication = theHandler.getAuthenticationForValidToken(
                                httpServletRequest, authorityList, emailAddress_userName);

                        //set the Authentication in the spring security context
                        SecurityContextHolder.getContext().setAuthentication(tokenSecurityAuthentication);
                    }
                } else {
                    //clear any traces of User Authentication is Spring Security Context
                    SecurityContextHolder.clearContext();
                }
                filterChain.doFilter(httpServletRequest, httpServletResponse); //pass the request along filter chain
            }
        } catch (NullPointerException ex) {
            System.out.println(ex);
            //if an exception occurs, send a response back to the user.
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            BangerAndCoExceptionHandler theException = new BangerAndCoExceptionHandler(
                    "authentication failed",
                    "Poor Values Received For Authentication",
                    HttpStatus.UNAUTHORIZED.value(),
                    null
            );

            //write JSON response to the client using Jackson Project
            new ObjectMapper().writer().writeValue(httpServletResponse.getOutputStream(), theException);
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

    private boolean canGrantAuthorization(GrantedAuthority USER_ROLE, String ENDPOINT) {
        boolean canAccess = false; //initially cannot access endpoint

        if (USER_ROLE.getAuthority().equals("ROLE_CUSTOMER")) {
            //if role is customer and only if endpoint is "/api/customer" allow access
            canAccess = ENDPOINT.contains("/api/customer");
        } else if (USER_ROLE.getAuthority().equals("ROLE_ADMINISTRATOR")) {
            //if role is administrator and only if endpoint is "/api/administrator" allow access
            canAccess = ENDPOINT.contains("/api/administrator");
        }

        return canAccess;
    }
}
