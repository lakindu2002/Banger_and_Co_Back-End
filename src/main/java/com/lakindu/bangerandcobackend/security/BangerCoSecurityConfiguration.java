package com.lakindu.bangerandcobackend.security;

import com.lakindu.bangerandcobackend.auth.JWTAuthFilter;
import com.lakindu.bangerandcobackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration //configuration class for bean definition methods
@EnableWebSecurity //enable Spring Security for Web
public class BangerCoSecurityConfiguration extends WebSecurityConfigurerAdapter {
    //WebSecurityConfigurerAdapter has methods used to configure Web Security

    private final JWTAuthFilter theAuthFilter;
    private final UserService theUserDetailsServiceImpl;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public BangerCoSecurityConfiguration(
            JWTAuthFilter theAuthFilter,
            UserService theUserDetailsServiceImpl,
            @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder
    ) {
        this.theAuthFilter = theAuthFilter;
        this.theUserDetailsServiceImpl = theUserDetailsServiceImpl;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //used to configure the authentication manager for user authentication
        auth.userDetailsService(theUserDetailsServiceImpl).passwordEncoder(passwordEncoder);
    }

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
                //ensure "Customer" and "Administrator" endpoints are authenticated for anyone who authorizes as "CUSTOMER"
                //or "ADMINISTRATOR"
                .antMatchers("/api/customer/**").hasAuthority("CUSTOMER")
                .antMatchers("/api/administrator/**").hasAuthority("ADMINISTRATOR")
                .anyRequest() //any other request
                .authenticated() //must be authenticated
                .and()
                //add a filter before UsernamePasswordAuthenticationFilter
                .addFilterAfter(theAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                //set session handling to Stateless
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

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

    @Bean
    @Override
    //create bean of authentication manager so it can be injected via CDI
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
