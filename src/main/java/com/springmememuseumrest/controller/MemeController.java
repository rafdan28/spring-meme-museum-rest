package com.springmememuseumrest.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.service.storage.ImageStorageService;

@RestController
@RequestMapping("/api/memes")
@RequiredArgsConstructor
public class MemeController {

    @Autowired
    private ImageStorageService imageStorageService;

    @PostMapping
    @Transactional
    public ResponseEntity<?> uploadMeme(
            @RequestParam("title") String title,
            @RequestParam("tags") List<String> tags,
            @RequestParam("image") MultipartFile image
    ) {
        try {
           imageStorageService.uploadImage(image);

            return ResponseEntity.ok("Meme caricato con successo!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore durante l'upload del meme.");
        }
    }
}