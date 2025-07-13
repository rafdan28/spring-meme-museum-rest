package com.springmememuseumrest.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openapispec.model.ApiMemesIdVotePostRequest;
import org.openapispec.model.MemeResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.springmememuseumrest.config.exception.ResourceNotFoundException;
import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.Tag;
import com.springmememuseumrest.model.User;
import com.springmememuseumrest.model.Vote;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.TagRepository;
import com.springmememuseumrest.repository.UserRepository;
import com.springmememuseumrest.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemeServiceImplementation implements MemeService {

    private final MemeRepository memeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final VoteRepository voteRepository;
    private final MemeMapper memeMapper;
    private final ImageStorageService imageStorageService;

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

        // Recupera utente autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        } else {
            currentUser = null;
        }

        // Mappa i risultati includendo il voto dell'utente
        List<MemeResponse> result = memePage.stream()
                .map(meme -> memeMapper.toModel(meme, currentUser))
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
            imageUrl = imageStorageService.uploadImage(image, "memes/");
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

    @Override
    public ResponseEntity<Void> setVote(Integer id, ApiMemesIdVotePostRequest voteRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meme non trovato"));

        Vote.VoteType newVoteType = voteRequest.getValue().getValue() == 1 
            ? Vote.VoteType.UPVOTE 
            : Vote.VoteType.DOWNVOTE;

        Optional<Vote> existingVoteOpt = voteRepository.findByUserAndMeme(user, meme);

        if (existingVoteOpt.isPresent()) {
            Vote existingVote = existingVoteOpt.get();
            if (existingVote.getType() == newVoteType) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                existingVote.setType(newVoteType);
                voteRepository.save(existingVote);
                return ResponseEntity.ok().build();
            }
        }

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setMeme(meme);
        vote.setType(newVoteType);
        voteRepository.save(vote);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteVote(
        Integer id
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        Vote vote = voteRepository.findByUserAndMeme(user, meme)
            .orElseThrow(() -> new ResourceNotFoundException("Voto non trovato"));

        voteRepository.delete(vote);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MemeResponse> getMemeById(Integer id) {
       Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        // Recupera utente autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        } else {
            currentUser = null;
        }
       
       MemeResponse response = memeMapper.toModel(meme, currentUser);
       
       return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteMemeById(Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        if (!meme.getAuthor().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            imageStorageService.deleteImage(meme.getImageUrl());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        memeRepository.delete(meme);
        return ResponseEntity.noContent().build();
    }
    
}
