package com.airline.bakend.service;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.entity.*;
import com.airline.bakend.repository.PaymentRepository;
import com.airline.bakend.repository.SeatRepository;
import com.airline.bakend.repository.TicketRepository;
import org.springframework.stereotype.Component;

import java.util.List;

// Converts database entities into the DTOs we send to the browser.
@Component
public class Mapper {

    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    public Mapper(SeatRepository seatRepository, PaymentRepository paymentRepository,
                  TicketRepository ticketRepository) {
        this.seatRepository = seatRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
    }

    public UserDto toUserDto(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }

    public FlightDto toFlightDto(Flight f) {
        return new FlightDto(f.getId(), f.getFlightNumber(),
                f.getAirline().getId(), f.getAirline().getName(), f.getAirline().getCode(),
                f.getAircraft().getId(), f.getAircraft().getModel(),
                f.getOrigin().getId(), f.getOrigin().getCode(), f.getOrigin().getCity(),
                f.getDestination().getId(), f.getDestination().getCode(), f.getDestination().getCity(),
                f.getDurationMinutes());
    }

    public ScheduleDto toScheduleDto(FlightSchedule s) {
        Flight f = s.getFlight();
        long available = seatRepository.countByScheduleIdAndStatus(s.getId(), SeatStatus.AVAILABLE);
        return new ScheduleDto(s.getId(), f.getId(), f.getFlightNumber(),
                f.getAirline().getName(), f.getAirline().getCode(),
                f.getOrigin().getCode(), f.getOrigin().getCity(),
                f.getDestination().getCode(), f.getDestination().getCity(),
                s.getDepartureTime(), s.getArrivalTime(), f.getDurationMinutes(),
                s.getBasePrice(), available, s.getStatus().name());
    }

    public SeatDto toSeatDto(Seat s) {
        return new SeatDto(s.getId(), s.getSeatNumber(), s.getSeatClass(), s.getPrice(), s.getStatus().name());
    }

    public BookingDto toBookingDto(Booking b) {
        Payment p = paymentRepository.findByBooking(b).orElse(null);
        Ticket t = ticketRepository.findByBooking(b).orElse(null);
        FlightSchedule s = b.getSchedule();
        Flight f = s.getFlight();
        List<PassengerDto> passengers = b.getPassengers().stream()
                .map(bp -> new PassengerDto(bp.getPassenger().getFullName(), bp.getPassenger().getAge(),
                        bp.getPassenger().getGender(), bp.getSeatNumber()))
                .toList();
        return new BookingDto(b.getId(), b.getPnr(), b.getStatus().name(), b.getTotalAmount(),
                b.getCreatedAt(), b.getExpiresAt(),
                s.getId(), f.getFlightNumber(), f.getAirline().getName(),
                f.getOrigin().getCode(), f.getOrigin().getCity(),
                f.getDestination().getCode(), f.getDestination().getCity(),
                s.getDepartureTime(), s.getArrivalTime(),
                passengers,
                p == null ? null : p.getStatus().name(),
                p == null ? null : p.getMethod(),
                p == null ? null : p.getTransactionRef(),
                t == null ? null : t.getTicketNumber(),
                t == null ? null : t.getIssuedAt(),
                b.getUser().getFullName(), b.getUser().getEmail());
    }

    public PaymentDto toPaymentDto(Payment p) {
        Booking b = p.getBooking();
        return new PaymentDto(p.getId(), b.getId(), b.getPnr(), p.getAmount(), p.getMethod(),
                p.getStatus().name(), p.getTransactionRef(), p.getCreatedAt(), b.getUser().getEmail());
    }
}
