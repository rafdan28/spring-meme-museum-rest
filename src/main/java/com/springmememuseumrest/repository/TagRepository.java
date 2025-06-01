package com.springmememuseumrest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
}
