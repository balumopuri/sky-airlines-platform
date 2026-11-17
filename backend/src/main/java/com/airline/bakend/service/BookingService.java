package com.airline.bakend.service;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.entity.*;
import com.airline.bakend.exception.ApiException;
import com.airline.bakend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 10;
    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final FlightScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final Mapper mapper;

    public BookingService(BookingRepository bookingRepository, SeatRepository seatRepository,
                          FlightScheduleRepository scheduleRepository, UserRepository userRepository,
                          PaymentRepository paymentRepository, TicketRepository ticketRepository,
                          Mapper mapper) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.mapper = mapper;
    }

    // STEP 1: choose seats + passengers -> booking is PENDING and the seats are HELD for 10 minutes.
    @Transactional
    public BookingDto create(String email, BookingRequest req) {
        expireOldPending();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        FlightSchedule schedule = scheduleRepository.findById(req.scheduleId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Flight schedule not found"));
        if (schedule.getStatus() != ScheduleStatus.SCHEDULED
                || schedule.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This flight is not available for booking");
        }

        List<PassengerRequest> passengers = req.passengers();
        if (passengers.size() > 9) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum 9 passengers per booking");
        }
        List<Long> seatIds = passengers.stream().map(PassengerRequest::seatId).toList();
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The same seat was selected twice");
        }

        // Lock the seat rows. If another user is booking the same seat, we wait here until they finish.
        List<Seat> seats = seatRepository.lockSeats(seatIds, schedule.getId());
        if (seats.size() != seatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more seats are invalid for this flight");
        }
        Map<Long, Seat> seatById = seats.stream().collect(Collectors.toMap(Seat::getId, s -> s));

        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Seat " + seat.getSeatNumber() + " is no longer available. Please choose another seat.");
            }
            total = total.add(seat.getPrice());
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(total);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(HOLD_MINUTES));

        for (PassengerRequest p : passengers) {
            Seat seat = seatById.get(p.seatId());
            seat.setStatus(SeatStatus.HELD);

            Passenger passenger = new Passenger();
            passenger.setFullName(p.fullName().trim());
            passenger.setAge(p.age());
            passenger.setGender(p.gender());
            passenger.setIdDocumentNumber(p.idDocumentNumber());

            BookingPassenger bp = new BookingPassenger();
            bp.setBooking(booking);
            bp.setPassenger(passenger);
            bp.setSeat(seat);
            bp.setSeatNumber(seat.getSeatNumber());
            booking.getPassengers().add(bp);
        }
        seatRepository.saveAll(seats);
        bookingRepository.save(booking);
        return mapper.toBookingDto(booking);
    }

    // STEP 2: pay. Only a verified SUCCESS payment confirms the booking and creates the PNR + ticket.
    @Transactional
    public BookingDto pay(Long bookingId, String email, PaymentRequest req) {
        Booking booking = loadOwned(bookingId, email);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "This booking is not waiting for payment");
        }
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            releaseSeats(booking);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            return mapper.toBookingDto(booking);   // status FAILED: the seat hold expired
        }

        boolean success = simulatePayment(req);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setMethod(req.method().toUpperCase());
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setTransactionRef("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        paymentRepository.save(payment);

        if (success) {
            for (BookingPassenger bp : booking.getPassengers()) {
                bp.getSeat().setStatus(SeatStatus.BOOKED);
            }
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPnr(generatePnr());
            bookingRepository.save(booking);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setTicketNumber("TKT-" + booking.getPnr() + "-" + booking.getId());
            ticketRepository.save(ticket);
        } else {
            releaseSeats(booking);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
        }
        return mapper.toBookingDto(booking);
    }

    @Transactional
    public BookingDto cancel(Long bookingId, String email, boolean admin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!admin && !booking.getUser().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only confirmed bookings can be cancelled");
        }
        if (booking.getSchedule().getDepartureTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ApiException(HttpStatus.CONFLICT, "Cancellation is closed 2 hours before departure");
        }
        releaseSeats(booking);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        paymentRepository.findByBooking(booking).ifPresent(p -> {
            p.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(p);
        });
        return mapper.toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> myBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return bookingRepository.findByUserOrderByCreatedAtDesc(user).stream().map(mapper::toBookingDto).toList();
    }

    @Transactional(readOnly = true)
    public BookingDto get(Long id, String email, boolean admin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!admin && !booking.getUser().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        return mapper.toBookingDto(booking);
    }

    // Bookings that were never paid within 10 minutes are failed and their seats go back on sale.
    @Transactional
    public void expireOldPending() {
        List<Booking> old = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());
        for (Booking b : old) {
            releaseSeats(b);
            b.setStatus(BookingStatus.FAILED);
            bookingRepository.save(b);
        }
    }

    // ---------- helpers ----------

    private Booking loadOwned(Long id, String email) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUser().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        return booking;
    }

    private void releaseSeats(Booking booking) {
        for (BookingPassenger bp : booking.getPassengers()) {
            Seat seat = bp.getSeat();
            if (seat != null) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
                bp.setSeat(null);
            }
        }
    }

    // Simulated payment gateway (for the academic demo).
    // CARD: 16 digits, and any card ending in 0000 is DECLINED (use it to test a failed payment).
    // UPI: must look like name@bank.   NETBANKING: always succeeds.
    private boolean simulatePayment(PaymentRequest req) {
        String method = req.method().toUpperCase();
        switch (method) {
            case "CARD": {
                String digits = req.cardNumber() == null ? "" : req.cardNumber().replaceAll("\\s", "");
                return digits.matches("\\d{16}") && !digits.endsWith("0000");
            }
            case "UPI":
                return req.upiId() != null && req.upiId().matches("[\\w.\\-]+@\\w+");
            case "NETBANKING":
                return true;
            default:
                return false;
        }
    }

    private String generatePnr() {
        String pnr;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(PNR_CHARS.charAt(RANDOM.nextInt(PNR_CHARS.length())));
            pnr = sb.toString();
        } while (bookingRepository.existsByPnr(pnr));
        return pnr;
    }
}
