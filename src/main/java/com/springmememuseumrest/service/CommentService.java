package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.CommentResponse;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    List<CommentResponse> getCommentsForMeme(Long memeId);
    void addComment(Long memeId, String content, String username);
    void deleteComment(Long memeId, Long commentId, String username);
}
