package com.springmememuseumrest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(
        String name
    );
}
