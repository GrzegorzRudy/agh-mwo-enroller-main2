package com.company.enroller.security;

import com.company.enroller.persistence.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ParticipantService participantService;

    @Value("${security.secret}")
    private String secret;

    @Value("${security.issuer}")
    private String issuer;

    @Value("${security.token_expiration_in_seconds}")
    private int tokenExpiration; // Zmieniono na int

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(participantService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/participants").permitAll() // Umożliwia dostęp do /participants
                .antMatchers("/tokens").permitAll() // Umożliwia dostęp do /tokens
                .anyRequest().authenticated() // Wymaga autoryzacji dla wszystkich innych endpointów
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Ustawia politykę sesji na stateless

        // Zarejestruj filtr JWTAuthenticationFilter
        http.addFilterBefore(new JWTAuthenticationFilter(authenticationManager(), secret, issuer, tokenExpiration), UsernamePasswordAuthenticationFilter.class);

        // Zarejestruj filtr JWTAuthorizationFilter
        http.addFilter(new JWTAuthorizationFilter(authenticationManager(), secret));
    }
}
