package com.lakindu.bangerandcobackend.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration //configuration class for bean definition methods
@EnableWebSecurity //enable Spring Security for Web
public class BangerCoSecurityConfiguration extends WebSecurityConfigurerAdapter {
    //WebSecurityConfigurerAdapter has methods used to configure Web Security

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //configure method overridden to configure the http security for the application
        http
                .cors() //this uses a default bean name of "corsConfigurationSource".
                //to enable the usage of the default bean name, a custom cors config with default bean name is used
                .and() //and
                .csrf().disable() //disable CSRF (Cross Site Request Forgery)
                .authorizeRequests() //authorize requests
                .antMatchers("/api/guest/**") //for the Guest Endpoint
                .permitAll() //allow all requests
                .anyRequest() //any other request
                .authenticated(); //must be authenticated
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        //create a URL Based CORS Configurator
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //create a CORS Configuration Object
        final CorsConfiguration corsConfiguration = new CorsConfiguration();

        //configure the cors object
        corsConfiguration.setAllowCredentials(true);
        //allow requests from Angular Client
        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        //configure headers allowed via CORS
        corsConfiguration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        //configure methods allowed from external domain
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        //set endpoint as base path to enable CORS access for whole API
        source.registerCorsConfiguration("/**", corsConfiguration);
        //create a FilterRegistrationBean and return it
        return source;
    }
}
