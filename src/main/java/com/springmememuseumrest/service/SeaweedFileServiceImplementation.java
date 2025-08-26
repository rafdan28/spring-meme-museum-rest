package com.springmememuseumrest.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.config.MultipartInputStreamFileResource;

@Service
public class SeaweedFileServiceImplementation implements SeaweedFileService {
    
    @Value("${seaweedfs.filer-url}")
    private String filerUrl;

    @Value("${seaweedfs.upload-path}")
    private String uploadPath;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String uploadFile(MultipartFile file, String typeFile, String directory) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        if (file.getContentType().startsWith("image/")) {
            directory += "image/";
        } else if (file.getContentType().startsWith("video/")) {
            directory += "video/";
        } else {
            throw new IllegalArgumentException("Formato file non supportato: " + file.getContentType());
        }
        String fullPath = uploadPath + directory + filename;
        String uploadUrl = filerUrl + fullPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename(), file.getSize()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Upload failed: " + response.getStatusCode());
        }

        return fullPath;
    }

    @Override
    public void deleteFile(String fullPath) throws IOException {
        String deleteUrl = filerUrl + fullPath;

        ResponseEntity<String> response = restTemplate.exchange(
            deleteUrl,
            HttpMethod.DELETE,
            null,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Failed to delete file: " + response.getStatusCode());
        }
    }
}
