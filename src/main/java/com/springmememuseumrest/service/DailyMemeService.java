package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.MemeResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface DailyMemeService {
    ResponseEntity<MemeResponse> getMemeOfToday();
    ResponseEntity<List<MemeResponse>> getDailyMemeHistory(
        Pageable pageable
    );
}