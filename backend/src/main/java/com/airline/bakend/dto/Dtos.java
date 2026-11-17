package com.airline.bakend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// All request/response shapes (DTOs) in one place. We never send entities to the browser.
public class Dtos {

    // ---------- auth & profile ----------
    public record RegisterRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password,
            String phone) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, String role, String fullName, String email) {}

    public record UserDto(Long id, String fullName, String email, String phone, String role) {}

    public record ProfileRequest(@NotBlank String fullName, String phone) {}

    // ---------- master data ----------
    public record AirportRequest(
            @NotBlank @Size(min = 3, max = 3, message = "must be 3 letters") String code,
            @NotBlank String name, @NotBlank String city, @NotBlank String country) {}

    public record AirlineRequest(
            @NotBlank @Size(min = 2, max = 3, message = "must be 2-3 characters") String code,
            @NotBlank String name) {}

    public record AircraftRequest(
            @NotBlank String model,
            @Min(1) @Max(60) int totalRows,
            @Min(1) @Max(10) int seatsPerRow) {}

    // ---------- flights & schedules ----------
    public record FlightRequest(
            @NotBlank String flightNumber,
            @NotNull Long airlineId, @NotNull Long aircraftId,
            @NotNull Long originId, @NotNull Long destinationId,
            @Min(1) int durationMinutes) {}

    public record FlightDto(
            Long id, String flightNumber,
            Long airlineId, String airlineName, String airlineCode,
            Long aircraftId, String aircraftModel,
            Long originId, String originCode, String originCity,
            Long destinationId, String destinationCode, String destinationCity,
            int durationMinutes) {}

    public record ScheduleRequest(
            @NotNull Long flightId,
            @NotNull LocalDateTime departureTime,
            @NotNull @DecimalMin("1.0") BigDecimal basePrice) {}

    public record ScheduleDto(
            Long id, Long flightId, String flightNumber, String airlineName, String airlineCode,
            String originCode, String originCity, String destinationCode, String destinationCity,
            LocalDateTime departureTime, LocalDateTime arrivalTime, int durationMinutes,
            BigDecimal basePrice, long availableSeats, String status) {}

    public record SeatDto(Long id, String seatNumber, String seatClass, BigDecimal price, String status) {}

    // ---------- bookings & payments ----------
    public record PassengerRequest(
            @NotBlank String fullName,
            @NotNull @Min(0) @Max(120) Integer age,
            @NotBlank String gender,
            String idDocumentNumber,
            @NotNull Long seatId) {}

    public record BookingRequest(
            @NotNull Long scheduleId,
            @NotEmpty @Valid List<PassengerRequest> passengers) {}

    public record PaymentRequest(@NotBlank String method, String cardNumber, String upiId) {}

    public record PassengerDto(String fullName, Integer age, String gender, String seatNumber) {}

    public record BookingDto(
            Long id, String pnr, String status, BigDecimal totalAmount,
            LocalDateTime createdAt, LocalDateTime expiresAt,
            Long scheduleId, String flightNumber, String airlineName,
            String originCode, String originCity, String destinationCode, String destinationCity,
            LocalDateTime departureTime, LocalDateTime arrivalTime,
            List<PassengerDto> passengers,
            String paymentStatus, String paymentMethod, String transactionRef,
            String ticketNumber, LocalDateTime issuedAt,
            String userName, String userEmail) {}

    public record PaymentDto(
            Long id, Long bookingId, String pnr, BigDecimal amount, String method,
            String status, String transactionRef, LocalDateTime createdAt, String userEmail) {}

    public record StatsDto(
            long totalFlights, long totalUsers, long totalBookings, BigDecimal totalRevenue,
            List<ScheduleDto> upcomingFlights, List<BookingDto> recentBookings) {}
}
