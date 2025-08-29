package com.springmememuseumrest.controller;

import java.util.List;

import org.openapispec.api.AdminApi;
import org.openapispec.model.AdminUserResponse;
import org.openapispec.model.ApiAdminUsersUsernameRolesPostRequest;
import org.openapispec.model.MemeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.springmememuseumrest.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;

    @Override
    public ResponseEntity<List<AdminUserResponse>> apiAdminUsersGet() {
        return adminService.getAllUsersWithStats();
    }

    @Override
    public ResponseEntity<Void> apiAdminUsersUsernameDelete(String username) {
        return adminService.deleteUser(username);
    }

    @Override
    public ResponseEntity<List<MemeResponse>> apiAdminUsersUsernameMemesGet(String username, Integer page, Integer size, String sort, String order) {
        return adminService.getUserMemes(username, page, size, sort, order);
    }

    @Override
    public ResponseEntity<Void> apiAdminUsersUsernameRolesPost(String username, ApiAdminUsersUsernameRolesPostRequest roles) {
        return adminService.assignRoles(username, roles);
    }
}
