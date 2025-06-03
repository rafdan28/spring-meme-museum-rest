package com.springmememuseumrest.service;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.config.JwtConfig;
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
    public boolean usersRegister(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            return false;
        }

        User user = new User();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        userRepository.save(user);

        String token = jwtConfig.generateToken(user);

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken(token);
        jwtResponse.setTokenType("Bearer");
        return true;
    }
}
