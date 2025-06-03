package com.springmememuseumrest.service;

import org.openapispec.model.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    boolean usersRegister(RegisterRequest request);
}