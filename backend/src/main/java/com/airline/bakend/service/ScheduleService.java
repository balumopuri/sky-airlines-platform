package com.airline.bakend.service;

import com.airline.bakend.entity.Aircraft;
import com.airline.bakend.entity.Flight;
import com.airline.bakend.entity.FlightSchedule;
import com.airline.bakend.entity.Seat;
import com.airline.bakend.repository.FlightScheduleRepository;
import com.airline.bakend.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    private static final String LETTERS = "ABCDEFGHJK";

    private final FlightScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    public ScheduleService(FlightScheduleRepository scheduleRepository, SeatRepository seatRepository) {
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
    }

    // Creates a schedule AND all its seats (rows x seats-per-row of the aircraft).
    @Transactional
    public FlightSchedule create(Flight flight, LocalDateTime departure, BigDecimal basePrice) {
        FlightSchedule s = new FlightSchedule();
        s.setFlight(flight);
        s.setDepartureTime(departure);
        s.setArrivalTime(departure.plusMinutes(flight.getDurationMinutes()));
        s.setBasePrice(basePrice.setScale(2, RoundingMode.HALF_UP));
        s = scheduleRepository.save(s);

        Aircraft a = flight.getAircraft();
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= a.getTotalRows(); row++) {
            for (int c = 0; c < a.getSeatsPerRow(); c++) {
                boolean business = row <= 2 && a.getTotalRows() >= 12;
                Seat seat = new Seat();
                seat.setSchedule(s);
                seat.setSeatNumber(row + String.valueOf(LETTERS.charAt(c)));
                seat.setSeatClass(business ? "BUSINESS" : "ECONOMY");
                seat.setPrice(business
                        ? basePrice.multiply(new BigDecimal("2.0")).setScale(2, RoundingMode.HALF_UP)
                        : basePrice.setScale(2, RoundingMode.HALF_UP));
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
        return s;
    }
}
