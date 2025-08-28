package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.MemeResponse;
import org.openapispec.model.RecoverRequest;
import org.openapispec.model.RecoverResponse;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.entity.User;

@Service
public interface UserService {
    User getCurrentAuthenticatedUser();
    ResponseEntity<RegisterResponse> usersRegister(
        RegisterRequest registerRequest
    );
    JwtResponse userslogin(
        LoginRequest loginRequest
    );
    ResponseEntity<RecoverResponse> recoverCredentials(
        RecoverRequest recoverRequest
    );
    ResponseEntity<Void> changePassword(
        String newPassword
    );
    ResponseEntity<UserResponse> getUserData();
    ResponseEntity<UserResponse> updateUserData(
        String name,
        String surname,
        String email,
        MultipartFile image
    );
    ResponseEntity<List<MemeResponse>> getUserMemes(
        Integer page, 
        Integer size, 
        String sort, 
        String order
    );
}