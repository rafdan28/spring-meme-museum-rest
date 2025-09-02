package com.springmememuseumrest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.entity.Comment;
import com.springmememuseumrest.entity.Meme;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMemeOrderByCreatedAtAsc(Meme meme);
}
