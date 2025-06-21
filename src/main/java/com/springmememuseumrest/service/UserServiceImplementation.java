package com.springmememuseumrest.service;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.config.JwtConfig;
import com.springmememuseumrest.config.exception.UnauthorizedException;
import com.springmememuseumrest.mapper.UserMapper;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.repository.UserRepository;

@Service
public class UserServiceImplementation implements UserService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtConfig jwtConfig;
    private UserMapper userMapper;
    

    @Autowired
    public UserServiceImplementation(
        AuthenticationManager authenticationManager,
        UserRepository userRepository, 
        PasswordEncoder passwordEncoder, 
        JwtConfig jwtConfig, 
        UserMapper userMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
        this.userMapper = userMapper;
    }

    @Override
    public boolean usersRegister(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername()) || userRepository.existsByEmail(registerRequest.getEmail())) {
            return false;
        }

        User user = userMapper.toModel(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        userRepository.save(user);

        return true;
    }

    @Override
    public JwtResponse userslogin(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
         if (authentication.isAuthenticated()) {
            User user = userRepository
                .findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Utente non valido"));

            JwtResponse jwt = new JwtResponse();
            jwt.setAccessToken(jwtConfig.generateToken(user));
            jwt.setTokenType("JWT");
            return jwt;
        } else {
            throw new UnauthorizedException("Credenziali non valide");
        }
    }
}
