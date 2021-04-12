package com.lakindu.bangerandcobackend.auth;

import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {
    //this class is used to define the implementation of the UserDetails that can be used in Spring Security Context.
    //this class mainly handles the Spring Security User.


    private final User theUserEntity;

    public CustomUserPrincipal(User theUserEntity) {
        this.theUserEntity = theUserEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(String.format("ROLE_%s", theUserEntity.getUserRole().getRoleName()))
        );
    }

    @Override
    public String getPassword() {
        return theUserEntity.getUserPassword();
    }

    @Override
    public String getUsername() {
        return theUserEntity.getEmailAddress();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
