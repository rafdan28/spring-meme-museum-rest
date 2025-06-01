package com.springmememuseumrest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.Meme;

public interface MemeRepository extends JpaRepository<Meme, Long> {
    
}