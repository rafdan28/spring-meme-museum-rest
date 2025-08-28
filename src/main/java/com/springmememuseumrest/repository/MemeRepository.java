package com.springmememuseumrest.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.springmememuseumrest.entity.Meme;
import com.springmememuseumrest.entity.User;

public interface MemeRepository extends JpaRepository<Meme, Long>, JpaSpecificationExecutor<Meme>{
    Page<Meme> findDistinctByTags_NameIn(
        List<String> tags, 
        Pageable pageable
    );
    /** Seleziona tutti i meme NON usati negli ultimi 30 giorni */
    @Query("""
       SELECT m FROM Meme m
       WHERE m.lastUsedDate IS NULL
          OR m.lastUsedDate < :barrageDate
    """)
    List<Meme> findEligibleDailyMeme(
        @Param("barrageDate") LocalDate barrageDate
    );
    Page<Meme> findAllByAuthor(
        User author, 
        Pageable pageable
    );
}