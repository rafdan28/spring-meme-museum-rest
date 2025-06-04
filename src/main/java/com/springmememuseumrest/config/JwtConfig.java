package com.springmememuseumrest.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.springmememuseumrest.model.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String base64Secret;

    @Value("${jwt.expiration-ms:86400000}") // default: 1 giorno
    private long expirationMs;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        claims.put("surname", user.getSurname());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setHeaderParam("alg", "HS256")  
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret)), SignatureAlgorithm.HS256)
                .compact();
    }
}