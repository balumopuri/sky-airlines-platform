package com.airline.bakend.service;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.entity.FlightSchedule;
import com.airline.bakend.entity.ScheduleStatus;
import com.airline.bakend.exception.ApiException;
import com.airline.bakend.repository.FlightScheduleRepository;
import com.airline.bakend.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightService {

    private final FlightScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final BookingService bookingService;
    private final Mapper mapper;

    public FlightService(FlightScheduleRepository scheduleRepository, SeatRepository seatRepository,
                         BookingService bookingService, Mapper mapper) {
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.bookingService = bookingService;
        this.mapper = mapper;
    }

    public List<ScheduleDto> search(String from, String to, LocalDate date, int passengers) {
        if (from.equalsIgnoreCase(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Departure and destination must be different");
        }
        bookingService.expireOldPending();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = date.atStartOfDay();
        if (start.isBefore(now)) start = now;           // never show flights that already left
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return scheduleRepository
                .search(from.toUpperCase(), to.toUpperCase(), start, end, ScheduleStatus.SCHEDULED)
                .stream()
                .map(mapper::toScheduleDto)
                .filter(d -> d.availableSeats() >= passengers)
                .toList();
    }

    public ScheduleDto getSchedule(Long id) {
        return mapper.toScheduleDto(find(id));
    }

    public List<SeatDto> getSeats(Long scheduleId) {
        find(scheduleId);
        bookingService.expireOldPending();
        return seatRepository.findByScheduleIdOrderById(scheduleId).stream().map(mapper::toSeatDto).toList();
    }

    private FlightSchedule find(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Flight schedule not found"));
    }
}
