package com.airline.bakend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldCleanupJob {

    private final BookingService bookingService;

    public HoldCleanupJob(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedRate = 60000)   // every minute
    public void run() {
        bookingService.expireOldPending();
    }
}
