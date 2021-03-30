package com.lakindu.bangerandcobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
public class BangerandcobackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BangerandcobackendApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
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
        return new FilterRegistrationBean(new CorsFilter(source));
    }

}
