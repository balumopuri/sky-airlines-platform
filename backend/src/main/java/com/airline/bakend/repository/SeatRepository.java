package com.airline.bakend.repository;

import com.airline.bakend.entity.Seat;
import com.airline.bakend.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScheduleIdOrderById(Long scheduleId);

    long countByScheduleIdAndStatus(Long scheduleId, SeatStatus status);

    // Row-level lock (SELECT ... FOR UPDATE): two people can never grab the same seat at once.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids and s.schedule.id = :scheduleId")
    List<Seat> lockSeats(@Param("ids") List<Long> ids, @Param("scheduleId") Long scheduleId);
}
