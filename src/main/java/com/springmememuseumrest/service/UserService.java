package com.springmememuseumrest.service;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    boolean usersRegister(RegisterRequest registerRequest);
    JwtResponse userslogin(LoginRequest loginRequest);
    ResponseEntity<UserResponse> getUserData();
}