package com.springmememuseumrest.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.entity.DailyMeme;
import com.springmememuseumrest.entity.Meme;

public interface DailyMemeRepository extends JpaRepository<DailyMeme, Long> {
    Optional<DailyMeme> findByDate(
        LocalDate date
    );
    List<DailyMeme> findByMeme(Meme meme);
}