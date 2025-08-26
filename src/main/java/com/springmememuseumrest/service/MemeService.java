package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.ApiMemesIdVotePostRequest;
import org.openapispec.model.MemeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MemeService {
    public List<MemeResponse> getMemeList(
        List<String> tags,
        String title,
        String sort,
        String order,
        Integer page,
        Integer size
    );
    public ResponseEntity<Void> uploadMeme(
        String title,
        List<String> tags, 
        MultipartFile file
    );
    public ResponseEntity<Void> setVote(
        Integer id, 
        ApiMemesIdVotePostRequest voteRequest
    );
    public ResponseEntity<Void> deleteVote(
        Integer id
    );
    public ResponseEntity<MemeResponse> getMemeById(
        Integer id
    );
    public ResponseEntity<MemeResponse> updateMemeById(
        Integer id, 
        String title, 
        List<String> tags,
        MultipartFile file
    );
    public ResponseEntity<Void> deleteMemeById(
        Integer id
    );
}