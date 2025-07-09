package com.springmememuseumrest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapispec.model.CommentResponse;

import com.springmememuseumrest.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "author", expression = "java(comment.getAuthor().getUsername())")
    @Mapping(target = "createdAt", expression = "java(comment.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))")
    CommentResponse toDto(Comment comment);
}