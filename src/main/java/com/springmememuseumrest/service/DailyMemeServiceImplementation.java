package com.springmememuseumrest.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.mapper.DailyMemeMapper;
import com.springmememuseumrest.model.DailyMeme;
import com.springmememuseumrest.model.Meme;
import com.springmememuseumrest.model.Vote;
import com.springmememuseumrest.repository.DailyMemeRepository;
import com.springmememuseumrest.repository.MemeRepository;

import jakarta.transaction.Transactional;

@Service
public class DailyMemeServiceImplementation implements DailyMemeService {

    private static final double WEIGHT_COMMENT = 2.0;
    private static final double WEIGHT_UPVOTES = 1.0;
    private static final double WEIGHT_DOWNVOTES = -1.0;
    private static final double DECAY_HALF_LIFE_DAYS = 30.0;

    private DailyMemeRepository dailyMemeRepository;
    private MemeRepository memeRepository;
    private DailyMemeMapper dailyMemeMapper;

    @Autowired
    public DailyMemeServiceImplementation(
        DailyMemeRepository dailyMemeRepository,
        MemeRepository memeRepository, 
        DailyMemeMapper dailyMemeMapper
    ) {
        this.dailyMemeRepository = dailyMemeRepository;
        this.memeRepository = memeRepository;
        this.dailyMemeMapper = dailyMemeMapper;
    }

    @Transactional
    @Override
    public Meme getMemeOfToday() {
        LocalDate today = LocalDate.now();
        return dailyMemeRepository.findByDate(today)
                .map(dailyMemeMapper::toMeme)
                .orElseGet(() -> selectAndSaveDailyMeme(today));
    }

    @Override
    public Page<Meme> getDailyMemeHistory(Pageable pageable) {
        return dailyMemeRepository.findAll(pageable).map(dailyMemeMapper::toMeme);
    }

    private Meme selectAndSaveDailyMeme(LocalDate today) {

        LocalDate barrageDate = today.minusDays(30);
        List<Meme> eligibleDailyMeme = memeRepository.findEligibleDailyMeme(barrageDate);

        if (eligibleDailyMeme.isEmpty())
            throw new IllegalStateException("Nessun meme del giorno eleggibile");

        Meme chosenDailyMeme = weightedRandom(eligibleDailyMeme);

        // aggiorna lastUsedDate e salva
        chosenDailyMeme.setLastUsedDate(today);
        memeRepository.save(chosenDailyMeme);
        dailyMemeRepository.save(new DailyMeme(null, chosenDailyMeme, today, null));

        return chosenDailyMeme;
    }

    /* Estrazione casuale ponderata */
    private Meme weightedRandom(List<Meme> list) {
        List<Double> scores = list.stream().map(this::score).toList();
        double total = scores.stream().mapToDouble(Double::doubleValue).sum();

        double r = ThreadLocalRandom.current().nextDouble(total);
        double cum = 0;
        for (int i = 0; i < list.size(); i++) {
            cum += scores.get(i);
            if (cum >= r) return list.get(i);
        }
        return list.getLast();  // fallback
    }

    /* Punteggio con tag-penalty e decay */
    private double score(Meme meme) {
        long upvotes = meme.getVotes().stream().filter(v -> v.getType()==Vote.VoteType.UPVOTE).count();
        long downvotes = meme.getVotes().stream().filter(v -> v.getType()==Vote.VoteType.DOWNVOTE).count();
        int comments = meme.getComments().size();

        double raw = comments * WEIGHT_COMMENT + upvotes * WEIGHT_UPVOTES + downvotes * WEIGHT_DOWNVOTES;

        int tagSize = meme.getTags().size();
        double tagFactor = tagSize > 10 ? 0.8 : (tagSize < 5 ? 1.1 : 1.0);

        long days = ChronoUnit.DAYS.between(meme.getCreatedAt(), Instant.now());
        double decay = Math.exp(-days / DECAY_HALF_LIFE_DAYS);

        return raw * tagFactor * decay;
    }
    
}
