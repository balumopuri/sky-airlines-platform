package com.airline.bakend.controller;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.entity.*;
import com.airline.bakend.exception.ApiException;
import com.airline.bakend.repository.*;
import com.airline.bakend.service.BookingService;
import com.airline.bakend.service.Mapper;
import com.airline.bakend.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Every URL here starts with /api/admin and is protected: only users with role ADMIN can call it.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final FlightScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final BookingService bookingService;
    private final Mapper mapper;

    public AdminController(AirportRepository airportRepository, AirlineRepository airlineRepository,
                           AircraftRepository aircraftRepository, FlightRepository flightRepository,
                           FlightScheduleRepository scheduleRepository, SeatRepository seatRepository,
                           BookingRepository bookingRepository, PaymentRepository paymentRepository,
                           UserRepository userRepository, ScheduleService scheduleService,
                           BookingService bookingService, Mapper mapper) {
        this.airportRepository = airportRepository;
        this.airlineRepository = airlineRepository;
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.scheduleService = scheduleService;
        this.bookingService = bookingService;
        this.mapper = mapper;
    }

    // ---------------- dashboard ----------------
    @GetMapping("/stats")
    public StatsDto stats() {
        List<ScheduleDto> upcoming = scheduleRepository
                .findTop5ByDepartureTimeAfterAndStatusOrderByDepartureTimeAsc(LocalDateTime.now(), ScheduleStatus.SCHEDULED)
                .stream().map(mapper::toScheduleDto).toList();
        List<BookingDto> recent = bookingRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(mapper::toBookingDto).toList();
        return new StatsDto(flightRepository.count(), userRepository.count(), bookingRepository.count(),
                paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS), upcoming, recent);
    }

    // ---------------- airports ----------------
    @PostMapping("/airports")
    public Airport addAirport(@Valid @RequestBody AirportRequest r) {
        Airport a = new Airport();
        return airportRepository.save(fill(a, r));
    }

    @PutMapping("/airports/{id}")
    public Airport editAirport(@PathVariable("id") Long id, @Valid @RequestBody AirportRequest r) {
        Airport a = airportRepository.findById(id).orElseThrow(() -> notFound("Airport"));
        return airportRepository.save(fill(a, r));
    }

    @DeleteMapping("/airports/{id}")
    public void deleteAirport(@PathVariable("id") Long id) {
        airportRepository.deleteById(id);
    }

    private Airport fill(Airport a, AirportRequest r) {
        a.setCode(r.code().trim().toUpperCase());
        a.setName(r.name().trim());
        a.setCity(r.city().trim());
        a.setCountry(r.country().trim());
        return a;
    }

    // ---------------- airlines ----------------
    @PostMapping("/airlines")
    public Airline addAirline(@Valid @RequestBody AirlineRequest r) {
        Airline a = new Airline();
        a.setCode(r.code().trim().toUpperCase());
        a.setName(r.name().trim());
        return airlineRepository.save(a);
    }

    @PutMapping("/airlines/{id}")
    public Airline editAirline(@PathVariable("id") Long id, @Valid @RequestBody AirlineRequest r) {
        Airline a = airlineRepository.findById(id).orElseThrow(() -> notFound("Airline"));
        a.setCode(r.code().trim().toUpperCase());
        a.setName(r.name().trim());
        return airlineRepository.save(a);
    }

    @DeleteMapping("/airlines/{id}")
    public void deleteAirline(@PathVariable("id") Long id) {
        airlineRepository.deleteById(id);
    }

    // ---------------- aircraft ----------------
    @GetMapping("/aircraft")
    public List<Aircraft> aircraft() {
        return aircraftRepository.findAll();
    }

    @PostMapping("/aircraft")
    public Aircraft addAircraft(@Valid @RequestBody AircraftRequest r) {
        Aircraft a = new Aircraft();
        a.setModel(r.model().trim());
        a.setTotalRows(r.totalRows());
        a.setSeatsPerRow(r.seatsPerRow());
        return aircraftRepository.save(a);
    }

    @DeleteMapping("/aircraft/{id}")
    public void deleteAircraft(@PathVariable("id") Long id) {
        aircraftRepository.deleteById(id);
    }

    // ---------------- flights ----------------
    @GetMapping("/flights")
    public List<FlightDto> flights() {
        return flightRepository.findAll().stream().map(mapper::toFlightDto).toList();
    }

    @PostMapping("/flights")
    public FlightDto addFlight(@Valid @RequestBody FlightRequest r) {
        if (flightRepository.existsByFlightNumber(r.flightNumber().trim().toUpperCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "Flight number already exists");
        }
        return mapper.toFlightDto(flightRepository.save(fill(new Flight(), r)));
    }

    @PutMapping("/flights/{id}")
    public FlightDto editFlight(@PathVariable("id") Long id, @Valid @RequestBody FlightRequest r) {
        Flight f = flightRepository.findById(id).orElseThrow(() -> notFound("Flight"));
        return mapper.toFlightDto(flightRepository.save(fill(f, r)));
    }

    @DeleteMapping("/flights/{id}")
    public void deleteFlight(@PathVariable("id") Long id) {
        if (scheduleRepository.existsByFlightId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "This flight has schedules. Cancel/remove them first.");
        }
        flightRepository.deleteById(id);
    }

    private Flight fill(Flight f, FlightRequest r) {
        if (r.originId().equals(r.destinationId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Origin and destination must be different");
        }
        f.setFlightNumber(r.flightNumber().trim().toUpperCase());
        f.setAirline(airlineRepository.findById(r.airlineId()).orElseThrow(() -> notFound("Airline")));
        f.setAircraft(aircraftRepository.findById(r.aircraftId()).orElseThrow(() -> notFound("Aircraft")));
        f.setOrigin(airportRepository.findById(r.originId()).orElseThrow(() -> notFound("Origin airport")));
        f.setDestination(airportRepository.findById(r.destinationId()).orElseThrow(() -> notFound("Destination airport")));
        f.setDurationMinutes(r.durationMinutes());
        return f;
    }

    // ---------------- schedules & seats ----------------
    @GetMapping("/schedules")
    public List<ScheduleDto> schedules() {
        return scheduleRepository.findAllByOrderByDepartureTimeDesc().stream().map(mapper::toScheduleDto).toList();
    }

    @PostMapping("/schedules")
    public ScheduleDto addSchedule(@Valid @RequestBody ScheduleRequest r) {
        Flight f = flightRepository.findById(r.flightId()).orElseThrow(() -> notFound("Flight"));
        if (r.departureTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Departure time must be in the future");
        }
        return mapper.toScheduleDto(scheduleService.create(f, r.departureTime(), r.basePrice()));
    }

    @PutMapping("/schedules/{id}/cancel")
    public ScheduleDto cancelSchedule(@PathVariable("id") Long id) {
        FlightSchedule s = scheduleRepository.findById(id).orElseThrow(() -> notFound("Schedule"));
        s.setStatus(ScheduleStatus.CANCELLED);
        return mapper.toScheduleDto(scheduleRepository.save(s));
    }

    @GetMapping("/schedules/{id}/seats")
    public List<SeatDto> seats(@PathVariable("id") Long id) {
        return seatRepository.findByScheduleIdOrderById(id).stream().map(mapper::toSeatDto).toList();
    }

    // Block (or unblock) a seat so customers cannot choose it. Only AVAILABLE seats can be blocked.
    @PutMapping("/seats/{id}/block")
    public SeatDto blockSeat(@PathVariable("id") Long id, @RequestParam("blocked") boolean blocked) {
        Seat seat = seatRepository.findById(id).orElseThrow(() -> notFound("Seat"));
        if (blocked && seat.getStatus() == SeatStatus.AVAILABLE) {
            seat.setStatus(SeatStatus.BLOCKED);
        } else if (!blocked && seat.getStatus() == SeatStatus.BLOCKED) {
            seat.setStatus(SeatStatus.AVAILABLE);
        } else {
            throw new ApiException(HttpStatus.CONFLICT, "Seat is " + seat.getStatus() + ", cannot change it");
        }
        return mapper.toSeatDto(seatRepository.save(seat));
    }

    // ---------------- bookings & payments ----------------
    @GetMapping("/bookings")
    public List<BookingDto> bookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toBookingDto).toList();
    }

    @PostMapping("/bookings/{id}/cancel")
    public BookingDto cancelBooking(@PathVariable("id") Long id, Authentication auth) {
        return bookingService.cancel(id, auth.getName(), true);
    }

    @GetMapping("/payments")
    public List<PaymentDto> payments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toPaymentDto).toList();
    }

    private ApiException notFound(String what) {
        return new ApiException(HttpStatus.NOT_FOUND, what + " not found");
    }
}
