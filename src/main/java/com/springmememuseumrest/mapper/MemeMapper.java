package com.springmememuseumrest.mapper;

import io.micrometer.common.lang.Nullable;

import com.springmememuseumrest.model.Vote;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.Tag;
import com.springmememuseumrest.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapispec.model.MemeResponse;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MemeMapper {

    @Mapping(target = "id", source = "meme.id")
    @Mapping(target = "tags", expression = "java(toTagNames(meme.getTags()))")
    @Mapping(target = "author", expression = "java(meme.getAuthor().getUsername())")
    @Mapping(target = "createdAt", expression = "java(toRomeOffset(meme.getCreatedAt()))")
    @Mapping(target = "upvotes", expression = "java(countUpvotes(meme))")
    @Mapping(target = "downvotes", expression = "java(countDownvotes(meme))")
    @Mapping(target = "comments", expression = "java(countComments(meme))")
    @Mapping(target = "userVote", expression = "java(resolveUserVote(meme, currentUser))")
    MemeResponse toModel(Meme meme, @Nullable User currentUser);

    default List<String> toTagNames(List<Tag> tags) {
        return tags.stream().map(Tag::getName).collect(Collectors.toList());
    }

    default int countUpvotes(Meme meme) {
        return (int) meme.getVotes().stream()
            .filter(v -> v.getType() == Vote.VoteType.UPVOTE)
            .count();
    }

    default int countDownvotes(Meme meme) {
        return (int) meme.getVotes().stream()
            .filter(v -> v.getType() == Vote.VoteType.DOWNVOTE)
            .count();
    }

    default int countComments(Meme meme) {
        return (int) meme.getComments().stream()
            .count();
    }

    default JsonNullable<Integer> resolveUserVote(Meme meme, @Nullable User currentUser) {
        if (currentUser == null) return JsonNullable.undefined();

        Optional<Integer> vote = meme.getVotes().stream()
            .filter(v -> v.getUser().getId().equals(currentUser.getId()))
            .findFirst()
            .map(v -> v.getType() == Vote.VoteType.UPVOTE ? 1 : -1);

        return vote.isPresent()
            ? JsonNullable.of(vote.get())
            : JsonNullable.undefined();
    }

    default OffsetDateTime toRomeOffset(Instant instant) {
        return instant.atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime();
    }
}