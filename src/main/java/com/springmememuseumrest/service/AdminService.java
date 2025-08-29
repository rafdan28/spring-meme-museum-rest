package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.AdminUserResponse;
import org.openapispec.model.MemeResponse;
import org.openapispec.model.ApiAdminUsersUsernameRolesPostRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public interface AdminService {
    ResponseEntity<List<AdminUserResponse>> getAllUsersWithStats();
    ResponseEntity<Void> deleteUser(
        String username
    );
    ResponseEntity<List<MemeResponse>> getUserMemes(
        String username, 
        Integer page, 
        Integer size, 
        String sort,
        String order
    );
    ResponseEntity<Void> assignRoles(
        String username, 
        ApiAdminUsersUsernameRolesPostRequest roles
    );
}
