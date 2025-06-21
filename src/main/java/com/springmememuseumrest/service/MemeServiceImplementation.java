package com.springmememuseumrest.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openapispec.model.MemeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.Tag;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.TagRepository;
import com.springmememuseumrest.repository.UserRepository;

@Service
public class MemeServiceImplementation implements MemeService {

    private MemeRepository memeRepository;
    private UserRepository userRepository;
    private TagRepository tagRepository;
    private MemeMapper memeMapper;
    private ImageStorageService imageStorageService;

    @Autowired
    public MemeServiceImplementation(
        MemeRepository memeRepository, 
        MemeMapper memeMapper, 
        UserRepository userRepository, 
        ImageStorageService imageStorageService,
        TagRepository tagRepository     
    ) {
        this.memeRepository = memeRepository;
        this.memeMapper = memeMapper;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<MemeResponse> getMemeList(List<String> tags, String sort, String order, Integer page, Integer size) {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;

        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = "createdAt";

        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        Page<Meme> memePage = (tags != null && !tags.isEmpty())
            ? memeRepository.findDistinctByTags_NameIn(tags, pageable)
            : memeRepository.findAll(pageable);

        List<MemeResponse> result = memePage.stream()
                .map(memeMapper::toModel)
                .collect(Collectors.toList()); 

        if ("upvotes".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparingInt(MemeResponse::getUpvotes));
        } else if ("downvotes".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparingInt(MemeResponse::getDownvotes));
        }

        if ("desc".equalsIgnoreCase(order)) {
            Collections.reverse(result);
        }

        return result;
    }

    @Override
    public ResponseEntity<Void> uploadMeme(String title, List<String> tags, MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User author = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        String imageUrl = null;
        try {
            imageUrl = imageStorageService.uploadImage(image);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();       
        }

        List<Tag> tagList = tags.stream()
                .map(tag -> tagRepository.findByName(tag.toLowerCase())
                    .orElseGet(() -> tagRepository.save(new Tag(tag.toLowerCase()))))
                .toList();

        Meme meme = new Meme();
        meme.setTitle(title);
        meme.setImageUrl(imageUrl);
        meme.setTags(tagList);
        meme.setAuthor(author);

        memeRepository.save(meme);

        return ResponseEntity.status(HttpStatus.CREATED).build();                        
    }
    
}
