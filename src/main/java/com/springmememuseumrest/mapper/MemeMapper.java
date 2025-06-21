package com.springmememuseumrest.mapper;

import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.Vote;
import com.springmememuseumrest.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapispec.model.MemeResponse;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MemeMapper {

    @Mapping(target = "tags", expression = "java(toTagNames(meme.getTags()))")
    @Mapping(target = "author", expression = "java(meme.getAuthor().getUsername())")
    @Mapping(target = "createdAt", expression = "java(toRomeOffset(meme.getCreatedAt()))")
    @Mapping(target = "upvotes", expression = "java(countUpvotes(meme))")
    @Mapping(target = "downvotes", expression = "java(countDownvotes(meme))")
    MemeResponse toModel(Meme meme);

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

    default OffsetDateTime toRomeOffset(Instant instant) {
        return instant.atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime();
    }
}