package com.airline.bakend.repository;

import com.airline.bakend.entity.FlightSchedule;
import com.airline.bakend.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long> {

    @Query("select s from FlightSchedule s "
            + "where s.flight.origin.code = :origin and s.flight.destination.code = :dest "
            + "and s.departureTime >= :start and s.departureTime < :end "
            + "and s.status = :status order by s.departureTime")
    List<FlightSchedule> search(@Param("origin") String origin,
                                @Param("dest") String dest,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("status") ScheduleStatus status);

    List<FlightSchedule> findAllByOrderByDepartureTimeDesc();

    List<FlightSchedule> findTop5ByDepartureTimeAfterAndStatusOrderByDepartureTimeAsc(
            LocalDateTime time, ScheduleStatus status);

    boolean existsByFlightId(Long flightId);
}
