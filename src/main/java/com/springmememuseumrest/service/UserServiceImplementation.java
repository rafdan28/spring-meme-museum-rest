package com.springmememuseumrest.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RecoverRequest;
import org.openapispec.model.RecoverResponse;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;

    @Override
    public User getCurrentAuthenticatedUser() {
        final User currentUser;

        // Recupera utente autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        } else {
            currentUser = null;
        }
        return currentUser;
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
    public ResponseEntity<RecoverResponse> recoverCredentials(RecoverRequest recoverRequest) {
        String email = recoverRequest.getEmail();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RecoverResponse().message("Nessun utente trovato con questa email"));
        }

        User user = userOpt.get();

        String username = null;
        String tempPassword = null;

        if (Boolean.TRUE.equals(recoverRequest.getRecoverUsername())) {
            username = user.getUsername();
        }

        if (Boolean.TRUE.equals(recoverRequest.getRecoverPassword())) {
            tempPassword = UUID.randomUUID().toString().substring(0, 5);
            user.setPassword(passwordEncoder.encode(tempPassword));
            userRepository.save(user);
        }

        // Invia email
        emailService.sendPasswordRecoveryEmail(user.getEmail(), username, user.getName(), user.getSurname(), tempPassword);

        return ResponseEntity.ok(new RecoverResponse()
                .message("Se l'email esiste, le credenziali sono state inviate."));
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
        if (image != null && !image.isEmpty()) {
            try {
                // Rimuovi vecchia immagine se esiste
                String oldImagePath = user.getImageProfileUrl();
                if (oldImagePath != null && !oldImagePath.isBlank()) {
                    imageStorageService.deleteImage(oldImagePath);
                }

                // Carica nuova immagine
                String imageUrl = imageStorageService.uploadImage(image, "users/");
                user.setImageProfileUrl(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
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
