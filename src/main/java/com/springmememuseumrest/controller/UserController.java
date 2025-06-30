package com.springmememuseumrest.controller;

import org.openapispec.api.UserApi;
import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.springmememuseumrest.service.UserService;

@RestController
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<JwtResponse> apiUsersLoginPost(LoginRequest loginRequest) {
        JwtResponse jwt = userService.userslogin(loginRequest);
        return ResponseEntity.ok(jwt);
    }

    @Override
    public ResponseEntity<RegisterResponse> apiUsersRegisterPost(RegisterRequest registerRequest) {
        return userService.usersRegister(registerRequest);
    }

    @Override
    public ResponseEntity<UserResponse> apiUsersUserGet() {
        return userService.getUserData();
    } 
}
