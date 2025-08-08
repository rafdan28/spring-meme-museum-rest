package com.springmememuseumrest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
}
