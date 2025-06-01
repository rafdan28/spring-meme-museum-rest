package com.springmememuseumrest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springmememuseumrest.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
