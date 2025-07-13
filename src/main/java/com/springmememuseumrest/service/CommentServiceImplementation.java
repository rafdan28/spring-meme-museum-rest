package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.CommentResponse;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.config.exception.ResourceNotFoundException;
import com.springmememuseumrest.config.exception.UnauthorizedException;
import com.springmememuseumrest.mapper.CommentMapper;
import com.springmememuseumrest.model.Comment;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.repository.CommentRepository;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImplementation implements CommentService {
    private final MemeRepository memeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentResponse> getCommentsForMeme(Long memeId) {
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));
        return meme.getComments()
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public void addComment(Long memeId, String content, String username) {
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setMeme(meme);
        comment.setAuthor(author);

        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long memeId, Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Commento non trovato"));

        if (!comment.getMeme().getId().equals(memeId)) {
            throw new IllegalArgumentException("Il commento non appartiene al meme");
        }

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("Non sei autorizzato a eliminare questo commento");
        }

        commentRepository.delete(comment);
    }
}
