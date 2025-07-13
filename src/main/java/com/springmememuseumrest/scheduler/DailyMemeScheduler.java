package com.springmememuseumrest.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.springmememuseumrest.service.DailyMemeService;

@EnableScheduling
@Configuration
class DailyMemeScheduler {
    private final DailyMemeService service;
    DailyMemeScheduler(DailyMemeService s){this.service=s;}

    @Scheduled(cron = "0 0 0 * * *")   // ogni giorno alle 00:00 server time
    // @Scheduled(cron = "0 */1 * * * *") // ogni minuto (solo per dev)
    public void pickDaily() {
        System.out.println("🕛 Esecuzione scheduler: selezione meme del giorno");
        service.getMemeOfToday();
    }
}