package com.springmememuseumrest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.model.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndMeme(User user, Meme meme);
}
