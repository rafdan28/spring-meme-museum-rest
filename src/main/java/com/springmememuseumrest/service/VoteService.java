package com.springmememuseumrest.service;

import java.util.List;

import org.openapispec.model.VoteResponse;

public interface VoteService {
    List<VoteResponse> getVotesForMeme(Long memeId);
}
