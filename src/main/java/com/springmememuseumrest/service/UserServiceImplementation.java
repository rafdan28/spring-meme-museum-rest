package com.springmememuseumrest.service;

import java.io.IOException;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.config.JwtConfig;
import com.springmememuseumrest.config.exception.UnauthorizedException;
import com.springmememuseumrest.mapper.UserMapper;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.repository.UserRepository;

@Service
public class UserServiceImplementation implements UserService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private PasswordEncoder passwordEncoder;
    private JwtConfig jwtConfig;
    private UserMapper userMapper;
    

    @Autowired
    public UserServiceImplementation(
        AuthenticationManager authenticationManager,
        UserRepository userRepository, 
        ImageStorageService imageStorageService,
        PasswordEncoder passwordEncoder, 
        JwtConfig jwtConfig, 
        UserMapper userMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<RegisterResponse> usersRegister(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername()) || userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = userMapper.toModel(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        userRepository.save(user);

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setMessage("Utente registrato con successo");

        return ResponseEntity.ok(registerResponse);
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

    @Override
    public ResponseEntity<UserResponse> getUserData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setSurname(user.getSurname());
        userResponse.setEmail(user.getEmail());
        userResponse.setUsername(user.getUsername());
        userResponse.setRoles(user.getRoles());
        userResponse.setImageProfileUrl(user.getImageProfileUrl());

        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<UserResponse> updateUserData(
        String name,
        String surname,
        String email,
        MultipartFile image
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        if (name != null) user.setName(name);
        if (surname != null) user.setSurname(surname);
        if (email != null) user.setEmail(email);

        // Gestione immagine se fornita
        String imageUrl = null;
        try {
            imageUrl = imageStorageService.uploadImage(image, "users/");
            user.setImageProfileUrl(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();       
        }

        userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles());
        response.setImageProfileUrl(user.getImageProfileUrl());

    return ResponseEntity.ok(response);
    }
}
