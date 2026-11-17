package com.airline.bakend.repository;

import com.airline.bakend.entity.Booking;
import com.airline.bakend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByBooking(Booking booking);
}
