package com.springmememuseumrest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@Service
public interface SeaweedFileService{
    public String uploadFile(
        MultipartFile image,
        String typeFile,
        String directory
    ) throws IOException;
    public void deleteFile(
        String fullPath
    ) throws IOException;
}
