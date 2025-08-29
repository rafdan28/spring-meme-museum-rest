package com.springmememuseumrest.service;

import java.util.ArrayList;
import java.util.List;

import org.openapispec.model.AdminUserResponse;
import org.openapispec.model.ApiAdminUsersUsernameRolesPostRequest;
import org.openapispec.model.MemeResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.config.exception.ResourceNotFoundException;
import com.springmememuseumrest.entity.User;
import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.repository.DailyMemeRepository;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImplementation implements AdminService {

    private final UserRepository userRepository;
    private final MemeRepository memeRepository;
    private final DailyMemeRepository dailyMemeRepository;
    private final MemeMapper memeMapper;

    @Override
    public ResponseEntity<List<AdminUserResponse>> getAllUsersWithStats() {
        List<AdminUserResponse> usersList = userRepository.findAll().stream()
                .map(user -> new AdminUserResponse()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .memeCount(memeRepository.countByAuthor(user))
                    .dailyMemeCount(dailyMemeRepository.countByMeme_Author(user))
                )
                .toList();
        
        return ResponseEntity.ok(usersList);
    }

    @Override
    public ResponseEntity<Void> deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<MemeResponse>> getUserMemes(String username, Integer page, Integer size, String sort,
            String order) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = (sort != null && !sort.isEmpty()) ? sort : "createdAt";

        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;

        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        List<MemeResponse> memeList =  memeRepository.findAllByAuthor(user, pageable).stream()
                .map(meme -> memeMapper.toModel(meme, user))
                .toList();
                
        return ResponseEntity.ok(memeList);
    }

    @Override
    public ResponseEntity<Void> assignRoles(String username, ApiAdminUsersUsernameRolesPostRequest roles) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        List<String> rolesList = roles.getRoles();
        if (rolesList == null || rolesList.isEmpty()) {
            user.setRoles(null);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }

        // Normalizzo in maiuscolo
        List<String> normalized = rolesList.stream()
                .map(String::toUpperCase)
                .toList();

        // Ruoli ammessi
        List<String> validRoles = List.of("USER", "ADMIN");
        if (!normalized.stream().allMatch(validRoles::contains)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        user.setRoles(new ArrayList<>(normalized));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    
}
