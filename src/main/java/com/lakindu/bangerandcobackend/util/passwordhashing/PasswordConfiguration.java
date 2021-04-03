package com.lakindu.bangerandcobackend.util.passwordhashing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
//declaration of beans
public class PasswordConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        //mean declaration to get a concrete implementation of the PasswordEncoder.
        return new BCryptPasswordEncoder(10); //return instance of BCryptPasswordEncoder
        //strength of 10 to denote 10 iterations. 2 to the power of 10 (default strength)
    }
}
