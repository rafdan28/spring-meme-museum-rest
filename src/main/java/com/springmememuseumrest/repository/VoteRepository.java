package com.springmememuseumrest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    
}
