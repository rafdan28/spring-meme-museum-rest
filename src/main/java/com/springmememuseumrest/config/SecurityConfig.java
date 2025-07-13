package com.springmememuseumrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET,  "/api/memes/{id:[\\d]+}").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/memes/{id:[\\d]+}/vote").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/memes/{id:[\\d]+}/comment").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/memes").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/memes/daily").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/memes/daily/history").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/tags/**").permitAll()
                .requestMatchers("/", "/index.html", "/css/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/api/users/login",
                                "/api/users/register").permitAll()
                // .requestMatchers(
                //     "/", "/index.html", "/css/**", 
                //     "/v3/api-docs/**", 
                //     "/swagger-ui.html", "/swagger-ui/**",
                //     "/api/users/login", 
                //     "/api/users/register",
                //     "/api/memes", // solo GET su /api/memes è pubblico
                //     "/api/memes/daily",
                //     "/api/memes/{id:[\\d]+}",
                //     "/api/memes/{id:[\\d]+}/comment",
                //     "/api/memes/{id:[\\d]+}/vote",
                //     "/api/tags/**"
                // ).permitAll()
                .anyRequest().authenticated()
            )
            // Stateless session (required for JWT)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Set custom authentication provider
            .authenticationProvider(authenticationProvider())
            // Add JWT filter before Spring Security's default filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* 
     * Authentication provider configuration
     * Links UserDetailsService and PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /* 
     * Authentication manager bean
     * Required for programmatic authentication
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}