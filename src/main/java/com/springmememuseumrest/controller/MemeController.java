package com.springmememuseumrest.controller;

import java.util.List;

import org.openapispec.api.MemeApi;
import org.openapispec.model.ApiMemesIdVotePostRequest;
import org.openapispec.model.CommentRequest;
import org.openapispec.model.CommentResponse;
import org.openapispec.model.MemeResponse;
import org.openapispec.model.VoteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.service.CommentService;
import com.springmememuseumrest.service.DailyMemeService;
import com.springmememuseumrest.service.MemeService;
import com.springmememuseumrest.service.VoteService;

@RestController
public class MemeController implements MemeApi {
    
    private MemeService memeService;
    private VoteService voteService;
    private CommentService commentService;
    private DailyMemeService dailyMemeService;
    private MemeMapper memeMapper;

    @Autowired
    public MemeController(
        MemeService memeService, 
        VoteService voteService,
        CommentService commentService,
        DailyMemeService dailyMemeService,
        MemeMapper memeMapper
    ) {
        this.memeService = memeService;
        this.voteService = voteService;
        this.commentService = commentService;
        this.dailyMemeService = dailyMemeService;
        this.memeMapper = memeMapper;
    }

    @Override
    public ResponseEntity<List<MemeResponse>> apiMemesGet(
        List<String> tags,
        String sort,
        String order,
        Integer page,
        Integer size
    ) {
        List<MemeResponse> result = memeService.getMemeList(tags, sort, order, page, size);
        return ResponseEntity.ok(result);
    }
    
    @Override
    public ResponseEntity<Void> apiMemesPost(
        @RequestParam("title") String title,
        @RequestParam("tags") List<String> tags,
        @RequestPart("image") MultipartFile image
    ) {
        return memeService.uploadMeme(title, tags, image);
    }

    @Override
    public ResponseEntity<Void> apiMemesIdVotePost(
        Integer id, 
        ApiMemesIdVotePostRequest voteRequest
    ) {
        return memeService.setVote(id, voteRequest);
    }

    @Override
    public ResponseEntity<Void> apiMemesIdVoteDelete(
        Integer id
    ) {
        return memeService.deleteVote(id);
    }

    @Override
    public ResponseEntity<List<VoteResponse>> apiMemesIdVoteGet(
        Integer id
    ) {
        List<VoteResponse> votes = voteService.getVotesForMeme(id.longValue());
        return ResponseEntity.ok(votes);
    }

    @Override
    public ResponseEntity<MemeResponse> apiMemesIdGet(
        Integer id
    ) {
        return memeService.getMemeById(id);
    }

    @Override
    public ResponseEntity<Void> apiMemesIdDelete(
        Integer id
    ) {
        return memeService.deleteMemeById(id);
    }

    @Override
    public ResponseEntity<List<CommentResponse>> apiMemesIdCommentGet(
        Integer id
    ) {
        List<CommentResponse> comments = commentService.getCommentsForMeme(id.longValue());
        return ResponseEntity.ok(comments);
    }

    @Override
    public ResponseEntity<Void> apiMemesIdCommentPost(
        Integer id, 
        CommentRequest request
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.addComment(id.longValue(), request.getContent(), username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> apiMemesIdCommentCommentIdDelete(
        Integer id, 
        Integer commentId
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.deleteComment(id.longValue(), commentId.longValue(), username);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MemeResponse> apiMemesDailyGet() {
        Meme meme = dailyMemeService.getMemeOfToday();
        return ResponseEntity.ok(memeMapper.toModel(meme, null));    
    }

    @Override
    public ResponseEntity<List<MemeResponse>> apiMemesDailyHistoryGet(Integer page, Integer size) {
        Page<Meme> pageObj = dailyMemeService.getDailyMemeHistory(
            PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by("date").descending()
            )
        );

        List<MemeResponse> list = pageObj.stream().map(meme -> memeMapper.toModel(meme, null)).toList();
        return ResponseEntity.ok(list);
    }
}