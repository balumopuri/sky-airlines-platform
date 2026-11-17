package com.airline.bakend.repository;

import com.airline.bakend.entity.Booking;
import com.airline.bakend.entity.BookingStatus;
import com.airline.bakend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findTop5ByOrderByCreatedAtDesc();
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);
    boolean existsByPnr(String pnr);
}
