package com.springmememuseumrest.service;

import com.springmememuseumrest.config.exception.ResourceNotFoundException;
import com.springmememuseumrest.entity.Meme;
import com.springmememuseumrest.entity.Vote;
import com.springmememuseumrest.mapper.VoteMapper;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.VoteRepository;
import lombok.RequiredArgsConstructor;

import org.openapispec.model.VoteResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImplementation implements VoteService {

    private final VoteRepository voteRepository;
    private final MemeRepository memeRepository;
    private final VoteMapper voteMapper;

    @Override
    public List<VoteResponse> getVotesForMeme(Long memeId) {
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        List<Vote> votes = voteRepository.findByMeme(meme);
        return votes.stream().map(voteMapper::toDto).collect(Collectors.toList());
    }
}