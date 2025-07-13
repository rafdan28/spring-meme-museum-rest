package com.springmememuseumrest.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.model.Meme;

@Service
public interface DailyMemeService {
    Meme getMemeOfToday();
    Page<Meme> getDailyMemeHistory(Pageable pageable);
}