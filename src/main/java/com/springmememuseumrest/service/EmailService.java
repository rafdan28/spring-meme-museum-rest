package com.springmememuseumrest.service;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    public void sendPasswordRecoveryEmail(
        String to, 
        String username, 
        String nome, 
        String cognome,
        String tempPassword
    );
}

