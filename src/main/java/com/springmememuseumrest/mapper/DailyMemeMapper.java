package com.springmememuseumrest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.springmememuseumrest.model.DailyMeme;
import com.springmememuseumrest.model.Meme;

@Mapper(componentModel = "spring")
public interface DailyMemeMapper {
    @Mapping(target = ".", source = "meme") 
    Meme toMeme(DailyMeme daily);

    List<Meme> toMemeList(List<DailyMeme> list);
}