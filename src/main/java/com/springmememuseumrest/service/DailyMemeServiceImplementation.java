package com.springmememuseumrest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.repository.DailyMemeRepository;
import com.springmememuseumrest.repository.MemeRepository;

@Service
public class DailyMemeServiceImplementation implements DailyMemeService {

    private DailyMemeRepository dailyMemeRepository;
    private MemeRepository memeRepository;

    @Autowired
    public DailyMemeServiceImplementation(
        DailyMemeRepository dailyMemeRepository,
        MemeRepository memeRepository
    ) {
        this.dailyMemeRepository = dailyMemeRepository;
        this.memeRepository = memeRepository;
    }
}
