package com.springmememuseumrest.service;

import com.springmememuseumrest.config.exception.ResourceNotFoundException;
import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.mapper.VoteMapper;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.Vote;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.VoteRepository;
import lombok.RequiredArgsConstructor;

import org.openapispec.model.VoteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImplementation implements VoteService {

    private VoteRepository voteRepository;
    private MemeRepository memeRepository;
    private VoteMapper voteMapper;

    @Autowired
    public VoteServiceImplementation(
        VoteRepository voteRepository,
        MemeRepository memeRepository,
        VoteMapper voteMapper
    ){
        this.voteRepository = voteRepository;
        this.memeRepository = memeRepository;  
        this.voteMapper = voteMapper;  
    }

    @Override
    public List<VoteResponse> getVotesForMeme(Long memeId) {
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        List<Vote> votes = voteRepository.findByMeme(meme);
        return votes.stream().map(voteMapper::toDto).collect(Collectors.toList());
    }
}