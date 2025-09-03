package com.springmememuseumrest.service;

import java.io.IOException;
import java.time.LocalDate;
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
import com.springmememuseumrest.entity.DailyMeme;
import com.springmememuseumrest.entity.Media;
import com.springmememuseumrest.entity.Meme;
import com.springmememuseumrest.entity.Tag;
import com.springmememuseumrest.entity.User;
import com.springmememuseumrest.entity.Vote;
import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.repository.DailyMemeRepository;
import com.springmememuseumrest.repository.MemeRepository;
import com.springmememuseumrest.repository.TagRepository;
import com.springmememuseumrest.repository.UserRepository;
import com.springmememuseumrest.repository.VoteRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemeServiceImplementation implements MemeService {

    private final MemeRepository memeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final VoteRepository voteRepository;
    private final DailyMemeRepository dailyMemeRepository;
    private final MemeMapper memeMapper;
    private final SeaweedFileService imageStorageService;
    private final UserService userService;


    @Override
    public List<MemeResponse> getMemeList(List<String> tags, String title, String sort, String order, Integer page, Integer size) {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;

        // Direzione
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Campo di ordinamento
        String sortBy;
        boolean manualSort = false;
        if ("upvotes".equalsIgnoreCase(sort) || "downvotes".equalsIgnoreCase(sort)) {
            sortBy = "createdAt"; // ordinamento DB fallback, poi ordiniamo in memoria
            manualSort = true;
        } else if ("createdAt".equalsIgnoreCase(sort)) {
            sortBy = "createdAt"; // ordinamento DB per data
        } else {
            sortBy = "createdAt"; // default
        }

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
                Join<Meme, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(cb.lower(tagJoin.get("name")).in(normalizedTags));

            } else if (!titleWords.isEmpty()) {
                Predicate titleAndPredicate = cb.conjunction();
                for (String word : titleWords) {
                    titleAndPredicate = cb.and(titleAndPredicate,
                            cb.like(cb.lower(root.get("title")), "%" + word + "%"));
                }
                Join<Meme, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                Predicate tagExactMatch = cb.lower(tagJoin.get("name")).in(titleWords);
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

        // Ordinamento manuale solo per upvotes/downvotes
        if (manualSort) {
            if ("upvotes".equalsIgnoreCase(sort)) {
                result.sort(Comparator.comparingInt(MemeResponse::getUpvotes));
            } else if ("downvotes".equalsIgnoreCase(sort)) {
                result.sort(Comparator.comparingInt(MemeResponse::getDownvotes));
            }
            if ("desc".equalsIgnoreCase(order)) {
                Collections.reverse(result);
            }
        }

        return result;
    }


    @Override
    public ResponseEntity<Void> uploadMeme(String title, List<String> tags, MultipartFile file) {
        String username = userService.getCurrentAuthenticatedUser().getUsername();

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        String fileUrl;
        try {
            fileUrl = imageStorageService.uploadFile(file, file.getContentType(), "memes/");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Normalizzo i tag: split su "," + trim + lowercase + elimino duplicati vuoti
        List<String> normalizedTags = tags.stream()
                .flatMap(tag -> Arrays.stream(tag.split(",")))
                .map(String::trim)                               // tolgo spazi
                .filter(t -> !t.isEmpty())                       // ignoro stringhe vuote
                .map(String::toLowerCase)
                .distinct()                                      // rimuovo duplicati
                .toList();

        List<Tag> tagList = normalizedTags.stream()
                .map(tag -> tagRepository.findByName(tag)
                        .orElseGet(() -> tagRepository.save(new Tag(tag))))
                .toList();

        Media media = new Media();
        media.setUrl(fileUrl);

        if (file.getContentType().startsWith("image/")) {
            media.setType("IMAGE");
        } else if (file.getContentType().startsWith("video/")) {
            media.setType("VIDEO");
        } else {
            throw new IllegalArgumentException("Formato file non supportato: " + file.getContentType());
        }

        media.setSize(file.getSize());

        Meme meme = new Meme();
        meme.setTitle(title);
        meme.setTags(tagList);
        meme.setAuthor(author);
        meme.setMedia(media);

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
    public ResponseEntity<Void> deleteVote(Integer id) {
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
    @Transactional
    public ResponseEntity<Void> deleteMemeById(Integer id) {
        String username = userService.getCurrentAuthenticatedUser().getUsername();

        Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        if (!meme.getAuthor().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Controlla se esiste in DailyMeme
        List<DailyMeme> dailyMemeList = dailyMemeRepository.findByMeme(meme);

        if (!dailyMemeList.isEmpty()) {
            LocalDate today = LocalDate.now();

            // Se è presente per la data odierna, blocca la cancellazione
            boolean isToday = dailyMemeList.stream()
                .anyMatch(dm -> dm.getDate().isEqual(today));

            if (isToday) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Altrimenti rimuovi tutte le entry vecchie
            dailyMemeRepository.deleteAll(dailyMemeList);
        }

        try {
            imageStorageService.deleteFile(meme.getMedia().getUrl());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        memeRepository.delete(meme);

        return ResponseEntity.noContent().build();
    }


    @Override
    public ResponseEntity<MemeResponse> updateMemeById(Integer id, String title, List<String> tags, MultipartFile file) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        Meme meme = memeRepository.findById(id.longValue())
            .orElseThrow(() -> new ResourceNotFoundException("Meme non trovato"));

        if (!meme.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Controlla se esiste in DailyMeme
        List<DailyMeme> dailyMemeList = dailyMemeRepository.findByMeme(meme);

        if (!dailyMemeList.isEmpty()) {
            LocalDate today = LocalDate.now();

            // Se è presente per la data odierna, blocca l'aggiornamento
            boolean isToday = dailyMemeList.stream()
                .anyMatch(dm -> dm.getDate().isEqual(today));

            if (isToday) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        if (title != null && !title.isEmpty()) {
            meme.setTitle(title);
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            if (file != null && !file.isEmpty()) {
                // Cancella file precedente da SeaweedFS
                String oldFilePath = meme.getMedia().getUrl();
                imageStorageService.deleteFile(oldFilePath);

                // Carica nuova immagine
                String newFilePath = imageStorageService.uploadFile(file, file.getContentType(), "memes/");

                Media media = new Media();
                media.setUrl(newFilePath);
                
                if (file.getContentType().startsWith("image/")) {
                    media.setType("IMAGE");
                } else if (file.getContentType().startsWith("video/")) {
                    media.setType("VIDEO");
                } else {
                    throw new IllegalArgumentException("Formato file non supportato: " + file.getContentType());
                }
        
                media.setSize(file.getSize());
                meme.setMedia(media);
            }
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();       
        }

        if (tags != null) {
            // Normalizzo i tag: split su "," + trim + lowercase + elimino duplicati vuoti
            List<String> normalizedTags = tags.stream()
                .flatMap(tag -> Arrays.stream(tag.split(","))) 
                .map(String::trim)                               // tolgo spazi
                .filter(t -> !t.isEmpty())                       // ignoro stringhe vuote
                .map(String::toLowerCase)
                .distinct()                                      // rimuovo duplicati
                .toList();

            List<Tag> tagList = normalizedTags.stream()
                .map(tag -> tagRepository.findByName(tag)
                    .orElseGet(() -> tagRepository.save(new Tag(tag))))
                .collect(Collectors.toCollection(ArrayList::new));

            meme.setTags(tagList);
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        memeRepository.save(meme);

        MemeResponse response = memeMapper.toModel(meme, currentUser);
       
        return ResponseEntity.ok(response);
    }
    
}
