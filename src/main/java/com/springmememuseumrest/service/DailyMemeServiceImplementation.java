package com.springmememuseumrest.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.openapispec.model.DailyMemeResponse;
import org.openapispec.model.MemeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springmememuseumrest.entity.DailyMeme;
import com.springmememuseumrest.entity.Meme;
import com.springmememuseumrest.entity.User;
import com.springmememuseumrest.entity.Vote;
import com.springmememuseumrest.mapper.DailyMemeMapper;
import com.springmememuseumrest.mapper.MemeMapper;
import com.springmememuseumrest.repository.DailyMemeRepository;
import com.springmememuseumrest.repository.MemeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyMemeServiceImplementation implements DailyMemeService {

    private static final double WEIGHT_COMMENT = 2.0;
    private static final double WEIGHT_UPVOTES = 1.0;
    private static final double WEIGHT_DOWNVOTES = -1.0;
    private static final double DECAY_HALF_LIFE_DAYS = 30.0; //quanto in fretta “invecchiano” i meme

    private final DailyMemeRepository dailyMemeRepository;
    private final MemeRepository memeRepository;
    private final DailyMemeMapper dailyMemeMapper;
    private final MemeMapper memeMapper;
    private final UserService userService;

    @Transactional
    @Override
    public ResponseEntity<MemeResponse> getMemeOfToday() {
        LocalDate today = LocalDate.now();
        Meme memeOfToday = dailyMemeRepository.findByDate(today)
                .map(dailyMemeMapper::toMeme)
                .orElseGet(() -> selectAndSaveDailyMeme(today)); //Estrae il meme del giorno
                                                                // se non presente nella data corrente

        // Recupera utente autenticato
        final User currentUser = userService.getCurrentAuthenticatedUser();

        return ResponseEntity.ok(memeMapper.toModel(memeOfToday, currentUser));
    }

    @Override
    public ResponseEntity<List<DailyMemeResponse>> getDailyMemeHistory(Pageable pageable) {
        Page<DailyMeme> page = dailyMemeRepository.findAll(pageable);

        // Recupera utente autenticato
        User currentUser = userService.getCurrentAuthenticatedUser();

        List<DailyMemeResponse> response = page.stream()
                .map(daily -> new DailyMemeResponse()
                    .meme(memeMapper.toModel(daily.getMeme(), currentUser))
                    .date(daily.getDate()))
                .toList();

    return ResponseEntity.ok(response);
    }

    private Meme selectAndSaveDailyMeme(LocalDate today) {
        //1. Calcola la data di sbarramento usata per recuperare i meme 
        //  non ancora eletti negli ultimi 30 giorni
        LocalDate barrageDate = today.minusDays(30);

        //2. Recupera i possibili meme del giorno NON usati negli ultimi 30 giorni
        List<Meme> eligibleDailyMemeList = memeRepository.findEligibleDailyMeme(barrageDate);

        if (eligibleDailyMemeList.isEmpty())
            throw new IllegalStateException("Nessun meme del giorno eleggibile");

        //3. Estra il meme del giorno
        Meme chosenDailyMeme = weightedRandom(eligibleDailyMemeList);
        chosenDailyMeme.setLastUsedDate(today);
        memeRepository.save(chosenDailyMeme);
        dailyMemeRepository.save(new DailyMeme(null, chosenDailyMeme, today, null));

        return chosenDailyMeme;
    }

    /* Estrazione meme in modo casuale ponderato */
    private Meme weightedRandom(List<Meme> eligibleDailyMemeList) {
        // 1. Calcola i punteggi di tutti i meme
        List<Double> scores = eligibleDailyMemeList.stream().map(this::score).toList();

        // 2. Calcola la somma dei punteggi
        double totalScore = scores.stream().mapToDouble(Double::doubleValue).sum();

        // 3. Se totalScore è 0 (cioè tutti i punteggi sono 0), seleziona un meme a caso
        if (totalScore <= 0 || scores.stream().allMatch(score -> score <= 0)) {
            return eligibleDailyMemeList.get(ThreadLocalRandom.current().nextInt(eligibleDailyMemeList.size()));
        }

        // 4. Estrai un numero casuale da [0, totalScore)
        double r = ThreadLocalRandom.current().nextDouble(totalScore);

        // 5. Selezione ponderata
        double tot = 0;
        for (int i = 0; i < eligibleDailyMemeList.size(); i++) {
            tot += scores.get(i);
            if (tot >= r) {
                return eligibleDailyMemeList.get(i);
            }
        }

        // 6. Fallback, non dovrebbe mai succedere
        return eligibleDailyMemeList.getLast();
    }

    /* Calcolo del punteggio per ogni meme con tag-penalty e decadimento temporale */
    private double score(Meme meme) {
        // 1. Conta upvote, downvote e commenti
        long upvotes = meme.getVotes().stream().filter(v -> v.getType() == Vote.VoteType.UPVOTE).count();
        long downvotes = meme.getVotes().stream().filter(v -> v.getType() == Vote.VoteType.DOWNVOTE).count();
        int comments = meme.getComments().size();
        
        // 2. Punteggio con pesi
        double scoreWeight = comments * WEIGHT_COMMENT          // commenti valgono di più
                            + upvotes * WEIGHT_UPVOTES          // upvotes hanno un peso medio
                            + downvotes * WEIGHT_DOWNVOTES;     // downvotes aggiungono una penalità

        // 3. Fattore tag‑penalty / bonus
        int tagSize = meme.getTags().size();
        double tagFactor = tagSize > 10 ? 0.8 :         // troppi tag danno un punteggio più basso
                           (tagSize < 5 ? 1.1 : 1.0);   // pochi tag < 5 danno un punteggio più alto rispetto
                                                        // ai tag compresi tra 5 e 10

        // 4. Decadimento temporale, i memi più vecchi di 30g valgono meno
        long days = ChronoUnit.DAYS.between(meme.getCreatedAt(), Instant.now());
        double decay = Math.exp(-days / DECAY_HALF_LIFE_DAYS);

        // 5. Calcolo del punteggio finale
        return scoreWeight * tagFactor * decay;
    }
    
}
