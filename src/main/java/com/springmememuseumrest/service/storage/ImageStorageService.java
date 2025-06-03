package com.springmememuseumrest.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ImageStorageService{
    
    @Value("${seaweedfs.filer-url}")
    private String filerUrl;

    @Value("${seaweedfs.upload-path}")
    private String uploadPath;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadImage(MultipartFile image) throws IOException {
        String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
        String fullPath = uploadPath + filename;
        String uploadUrl = filerUrl + fullPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(image.getInputStream(), image.getOriginalFilename(), image.getSize()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Upload failed: " + response.getStatusCode());
        }

        return fullPath; // questo path verrà poi salvato nell'entità Meme
    }
}
