package com.springmememuseumrest.service;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.config.JwtConfig;
import com.springmememuseumrest.config.exception.UnauthorizedException;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.repository.UserRepository;

@Service
public class UserServiceImplementation implements UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtConfig jwtConfig;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public boolean usersRegister(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername()) || userRepository.existsByEmail(registerRequest.getEmail())) {
            return false;
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setSurname(registerRequest.getSurname());
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        userRepository.save(user);

        return true;
    }

    @Override
    public JwtResponse userslogin(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if(user == null) throw new UnauthorizedException("Credenziali non valide");

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenziali non valide");
        }

        JwtResponse jwt = new JwtResponse();
        jwt.setAccessToken(jwtConfig.generateToken(user));
        jwt.setTokenType("JWT");
        return jwt;
    }
}
