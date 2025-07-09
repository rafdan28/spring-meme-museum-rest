package com.springmememuseumrest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@Service
public interface ImageStorageService{
    public String uploadImage(
        MultipartFile image,
        String type
    ) throws IOException;
    public void deleteImage(
        String fullPath
    ) throws IOException;
}
