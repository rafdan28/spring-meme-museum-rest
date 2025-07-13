package com.springmememuseumrest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.DailyMeme;

public interface DailyMemeRepository extends JpaRepository<DailyMeme, Long> {

}