package com.springmememuseumrest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openapispec.model.ApiMemesIdVotePostRequest;
import org.openapispec.model.MemeResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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
    private final UserService userService;


    @Override
    public List<MemeResponse> getMemeList(List<String> tags, String title, String sort, String order, Integer page, Integer size) {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;

        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = "createdAt";
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        // Normalizza tags
        List<String> normalizedTags = Optional.ofNullable(tags)
            .orElse(Collections.emptyList())
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        // Splitta titolo in parole
        List<String> titleWords = (title == null) ? Collections.emptyList() :
            Arrays.stream(title.split("\\W+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        Specification<Meme> spec = (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (!normalizedTags.isEmpty()) {
                // Cerca solo per tag
                Join<Meme, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(cb.lower(tagJoin.get("name")).in(normalizedTags));

            } else if (!titleWords.isEmpty()) {
                // Titolo deve contenere tutte le parole (AND)
                Predicate titleAndPredicate = cb.conjunction();
                for (String word : titleWords) {
                    titleAndPredicate = cb.and(titleAndPredicate,
                            cb.like(cb.lower(root.get("title")), "%" + word + "%"));
                }

                // Tag deve coincidere esattamente con almeno una parola
                Join<Meme, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                Predicate tagExactMatch = cb.lower(tagJoin.get("name")).in(titleWords);

                // Il match valido è: titolo che contiene tutte le parole OR tag che matcha esattamente
                predicates.add(cb.or(titleAndPredicate, tagExactMatch));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Meme> memePage;
        if (!normalizedTags.isEmpty()) {
            memePage = memeRepository.findDistinctByTags_NameIn(normalizedTags, pageable);
        } else if (!titleWords.isEmpty()) {
            memePage = memeRepository.findAll(spec, pageable);
        } else {
            memePage = memeRepository.findAll(pageable);
        }

        final User currentUser = userService.getCurrentAuthenticatedUser();

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
        String username = userService.getCurrentAuthenticatedUser().getUsername();

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
        String username = userService.getCurrentAuthenticatedUser().getUsername();

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
        String username = userService.getCurrentAuthenticatedUser().getUsername();

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
        final User currentUser = userService.getCurrentAuthenticatedUser();
       
        MemeResponse response = memeMapper.toModel(meme, currentUser);
       
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteMemeById(Integer id) {
        String username = userService.getCurrentAuthenticatedUser().getUsername();

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

    @Override
    public ResponseEntity<MemeResponse> updateMemeById(Integer id, String title, List<String> tags, MultipartFile image) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        if (!meme.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (title != null && !title.isEmpty()) {
            meme.setTitle(title);
        }

        try {
            if (image != null && !image.isEmpty()) {
                // Cancella immagine precedente da SeaweedFS
                String oldImagePath = meme.getImageUrl();
                imageStorageService.deleteImage(oldImagePath);

                // Carica nuova immagine
                String newImagePath = imageStorageService.uploadImage(image, "memes/");
                meme.setImageUrl(newImagePath);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();       
        }

        if (tags != null) {
            List<Tag> tagList = tags.stream()
                .map(tag -> tagRepository.findByName(tag.toLowerCase())
                    .orElseGet(() -> tagRepository.save(new Tag(tag.toLowerCase()))))
                .collect(Collectors.toCollection(ArrayList::new));
            meme.setTags(tagList);
        }

        memeRepository.save(meme);

        MemeResponse response = memeMapper.toModel(meme, currentUser);
       
        return ResponseEntity.ok(response);
    }
    
}
