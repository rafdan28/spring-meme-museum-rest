package com.springmememuseumrest.service;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface UserService {
    ResponseEntity<RegisterResponse> usersRegister(RegisterRequest registerRequest);
    JwtResponse userslogin(
        LoginRequest loginRequest
    );
    ResponseEntity<UserResponse> getUserData();
    ResponseEntity<UserResponse> updateUserData(
        String name,
        String surname,
        String email,
        MultipartFile image
    );
}