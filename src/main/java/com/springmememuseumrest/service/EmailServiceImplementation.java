package com.springmememuseumrest.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class EmailServiceImplementation implements EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordRecoveryEmail(String to, String username, String nome, String cognome, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Recupero credenziali - MEME MUSEUM");
        // message.setFrom("noreply@mememuseum.com");

        StringBuilder body = new StringBuilder();
        body.append("Ciao " + nome + " " + cognome + ",\n\n");
        body.append("Hai richiesto il recupero delle credenziali del tuo account MEME MUSEUM.\n\n");

        if (username != null) {
            body.append("👤 Username: ").append(username).append("\n");
        }
        if (tempPassword != null) {
            body.append("🔑 Password temporanea: ").append(tempPassword).append("\n");
            body.append("Ti consigliamo di cambiarla dopo il primo accesso.\n");
        }

        body.append("\n\nGrazie,\nIl team di MEME MUSEUM");

        message.setText(body.toString());

        mailSender.send(message);
    }
}
