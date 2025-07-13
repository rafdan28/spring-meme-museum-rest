package com.springmememuseumrest.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.DailyMeme;

public interface DailyMemeRepository extends JpaRepository<DailyMeme, Long> {
    Optional<DailyMeme> findByDate(LocalDate date);
}