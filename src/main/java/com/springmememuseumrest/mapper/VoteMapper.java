package com.springmememuseumrest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapispec.model.VoteResponse;

import com.springmememuseumrest.entity.Vote;

@Mapper(componentModel = "spring")
public interface VoteMapper {

    @Mapping(target = "type", expression = "java(mapVoteType(vote.getType()))")
    @Mapping(target = "author", source = "user.username")
    @Mapping(target = "authorImageUrl", expression = "java(vote.getUser().getImageProfileUrl())")
    @Mapping(target = "createdAt", expression = "java(vote.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))")
    VoteResponse toDto(Vote vote);

    default Integer mapVoteType(Vote.VoteType type) {
        return type == Vote.VoteType.UPVOTE ? 1 : -1;
    }
}