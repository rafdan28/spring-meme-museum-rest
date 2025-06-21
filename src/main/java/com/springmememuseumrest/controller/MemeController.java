package com.springmememuseumrest.controller;

import java.util.List;

import org.openapispec.api.MemeApi;
import org.openapispec.model.MemeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.service.MemeService;

@RestController
public class MemeController implements MemeApi {
    private final MemeService memeService;

    public MemeController(MemeService memeService) {
        this.memeService = memeService;
    }

    @Override
    public ResponseEntity<List<MemeResponse>> apiMemesGet(
        List<String> tags,
        String sort,
        String order,
        Integer page,
        Integer size
    ) {
        List<MemeResponse> result = memeService.getMemeList(tags, sort, order, page, size);
        return ResponseEntity.ok(result);
    }
    
    @Override
    public ResponseEntity<Void> apiMemesPost(
            @RequestParam("title") String title,
            @RequestParam("tags") List<String> tags,
            @RequestPart("image") MultipartFile image
    ) {
        return memeService.uploadMeme(title, tags, image);
    }
}