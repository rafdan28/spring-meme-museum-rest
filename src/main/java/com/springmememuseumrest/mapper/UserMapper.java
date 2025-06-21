package com.springmememuseumrest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapispec.model.RegisterRequest;

import com.springmememuseumrest.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "memes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "votes", ignore = true)
    User toModel(RegisterRequest registerRequest);

}
