package com.airline.bakend.controller;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingRequest req, Authentication auth) {
        return bookingService.create(auth.getName(), req);
    }

    @PostMapping("/{id}/pay")
    public BookingDto pay(@PathVariable("id") Long id, @Valid @RequestBody PaymentRequest req, Authentication auth) {
        return bookingService.pay(id, auth.getName(), req);
    }

    @GetMapping("/my")
    public List<BookingDto> my(Authentication auth) {
        return bookingService.myBookings(auth.getName());
    }

    @GetMapping("/{id}")
    public BookingDto get(@PathVariable("id") Long id, Authentication auth) {
        return bookingService.get(id, auth.getName(), isAdmin(auth));
    }

    @PostMapping("/{id}/cancel")
    public BookingDto cancel(@PathVariable("id") Long id, Authentication auth) {
        return bookingService.cancel(id, auth.getName(), false);
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
