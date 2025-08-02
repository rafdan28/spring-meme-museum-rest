package com.springmememuseumrest.controller;

import java.util.List;

import org.openapispec.api.UserApi;
import org.openapispec.model.ApiUsersPasswordPatchRequest;
import org.openapispec.model.JwtResponse;
import org.openapispec.model.LoginRequest;
import org.openapispec.model.MemeResponse;
import org.openapispec.model.RecoverRequest;
import org.openapispec.model.RecoverResponse;
import org.openapispec.model.RegisterRequest;
import org.openapispec.model.RegisterResponse;
import org.openapispec.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<RegisterResponse> apiUsersRegisterPost(
        RegisterRequest registerRequest
    ) {
        return userService.usersRegister(registerRequest);
    }

    @Override
    public ResponseEntity<JwtResponse> apiUsersLoginPost(
        LoginRequest loginRequest
    ) {
        JwtResponse jwt = userService.userslogin(loginRequest);
        return ResponseEntity.ok(jwt);
    }

    @Override
    public ResponseEntity<Void> apiUsersUsernameDelete(String username) {
        return userService.deleteUserByAdmin(username);
    }

    @Override
    public ResponseEntity<RecoverResponse> apiUsersRecoverPost(
        RecoverRequest recoverRequest
    ) {
        return userService.recoverCredentials(recoverRequest);
    }

    @Override
    public ResponseEntity<Void> apiUsersPasswordPatch(
        ApiUsersPasswordPatchRequest apiUsersPasswordPatchRequest
    ){
        return userService.changePassword(apiUsersPasswordPatchRequest.getNewPassword());
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

    @Override
    public ResponseEntity<List<MemeResponse>> apiUsersUserMemesGet() {
        return userService.getUserMemes();
    }
}
