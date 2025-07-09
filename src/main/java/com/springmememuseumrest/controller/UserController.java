package com.springmememuseumrest.controller;

import org.openapispec.api.UserApi;
import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.service.UserService;

@RestController
public class UserController implements UserApi {

    private UserService userService;

    @Autowired
    public UserController(
        UserService userService
    ) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<JwtResponse> apiUsersLoginPost(
        LoginRequest loginRequest
    ) {
        JwtResponse jwt = userService.userslogin(loginRequest);
        return ResponseEntity.ok(jwt);
    }

    @Override
    public ResponseEntity<RegisterResponse> apiUsersRegisterPost(
        RegisterRequest registerRequest
    ) {
        return userService.usersRegister(registerRequest);
    }

    @Override
    public ResponseEntity<UserResponse> apiUsersUserGet() {
        return userService.getUserData();
    } 

    @Override
    public ResponseEntity<UserResponse> apiUsersUserPatch(
            String name,
            String surname,
            String email,
            MultipartFile image
    ) {
        return userService.updateUserData(name, surname, email, image);
    }
}
