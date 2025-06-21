package com.springmememuseumrest.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.Meme;

public interface MemeRepository extends JpaRepository<Meme, Long> {
    Page<Meme> findDistinctByTags_NameIn(List<String> tags, Pageable pageable);
}