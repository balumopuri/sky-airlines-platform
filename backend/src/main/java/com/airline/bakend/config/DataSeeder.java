package com.airline.bakend.config;

import com.airline.bakend.entity.*;
import com.airline.bakend.repository.*;
import com.airline.bakend.service.ScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Runs once at startup. If the database is empty it inserts realistic sample data.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final ScheduleService scheduleService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, AirportRepository airportRepository,
                      AirlineRepository airlineRepository, AircraftRepository aircraftRepository,
                      FlightRepository flightRepository, ScheduleService scheduleService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.airportRepository = airportRepository;
        this.airlineRepository = airlineRepository;
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.scheduleService = scheduleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        if (airportRepository.count() == 0) {
            System.out.println(">>> Seeding sample flights (takes a few seconds)...");
            seedFlights();
            System.out.println(">>> Sample data ready.");
        }
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
        userRepository.save(user("Admin", "admin@airline.com", "admin123", Role.ADMIN));
        userRepository.save(user("Demo Customer", "demo@airline.com", "demo123", Role.CUSTOMER));
    }

    private User user(String name, String email, String password, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setPhone("9999999999");
        u.setRole(role);
        return u;
    }

    private void seedFlights() {
        Map<String, Airport> airports = new HashMap<>();
        String[][] ap = {
                {"DEL", "Indira Gandhi International", "Delhi"},
                {"BOM", "Chhatrapati Shivaji Maharaj International", "Mumbai"},
                {"BLR", "Kempegowda International", "Bengaluru"},
                {"MAA", "Chennai International", "Chennai"},
                {"HYD", "Rajiv Gandhi International", "Hyderabad"},
                {"CCU", "Netaji Subhas Chandra Bose International", "Kolkata"},
                {"GOI", "Dabolim Airport", "Goa"},
                {"PNQ", "Pune Airport", "Pune"}};
        for (String[] a : ap) {
            Airport x = new Airport();
            x.setCode(a[0]);
            x.setName(a[1]);
            x.setCity(a[2]);
            x.setCountry("India");
            airports.put(a[0], airportRepository.save(x));
        }

        Map<String, Airline> airlines = new HashMap<>();
        String[][] al = {{"6E", "IndiGo"}, {"AI", "Air India"}, {"SG", "SpiceJet"}, {"QP", "Akasa Air"}};
        for (String[] a : al) {
            Airline x = new Airline();
            x.setCode(a[0]);
            x.setName(a[1]);
            airlines.put(a[0], airlineRepository.save(x));
        }

        Aircraft a320 = aircraft("Airbus A320", 15, 6);
        Aircraft b737 = aircraft("Boeing 737-800", 18, 6);
        Aircraft atr = aircraft("ATR 72", 10, 4);
        Aircraft[] planes = {a320, b737, atr};

        // flight no, airline, from, to, departure time, duration (min), base price (INR), aircraft index
        Object[][] flights = {
                {"6E-201", "6E", "DEL", "BOM", "06:00", 125, 4500, 0},
                {"AI-101", "AI", "DEL", "BOM", "09:30", 135, 5800, 1},
                {"SG-301", "SG", "DEL", "BOM", "14:00", 130, 3900, 0},
                {"QP-401", "QP", "DEL", "BOM", "19:00", 130, 4300, 1},
                {"6E-202", "6E", "DEL", "BLR", "07:00", 170, 5100, 0},
                {"AI-103", "AI", "DEL", "BLR", "11:00", 175, 6400, 1},
                {"6E-203", "6E", "DEL", "CCU", "06:30", 135, 5300, 0},
                {"AI-105", "AI", "DEL", "MAA", "07:30", 175, 6100, 1},
                {"6E-204", "6E", "DEL", "HYD", "12:00", 140, 4800, 0},
                {"6E-205", "6E", "DEL", "GOI", "08:00", 165, 5600, 0},
                {"6E-211", "6E", "BOM", "DEL", "07:30", 130, 4600, 0},
                {"AI-111", "AI", "BOM", "DEL", "13:00", 140, 5900, 1},
                {"6E-212", "6E", "BOM", "BLR", "08:00", 100, 3800, 0},
                {"QP-412", "QP", "BOM", "BLR", "16:00", 105, 3600, 1},
                {"SG-311", "SG", "BOM", "GOI", "09:00", 70, 3200, 2},
                {"6E-214", "6E", "BOM", "HYD", "14:00", 90, 3500, 0},
                {"6E-221", "6E", "BLR", "DEL", "05:30", 165, 5200, 0},
                {"6E-222", "6E", "BLR", "MAA", "07:00", 65, 2800, 2},
                {"SG-321", "SG", "BLR", "HYD", "10:00", 80, 3100, 2},
                {"AI-121", "AI", "BLR", "BOM", "15:30", 105, 5100, 1},
                {"6E-231", "6E", "MAA", "HYD", "09:00", 75, 2900, 2},
                {"AI-131", "AI", "MAA", "DEL", "06:30", 175, 6300, 1},
                {"6E-251", "6E", "CCU", "DEL", "07:00", 140, 5500, 0},
                {"6E-261", "6E", "GOI", "DEL", "14:00", 160, 5700, 0},
                {"6E-271", "6E", "PNQ", "DEL", "07:00", 120, 4200, 0},
                {"6E-272", "6E", "DEL", "PNQ", "17:00", 120, 4200, 0}};

        LocalDate today = LocalDate.now();
        for (Object[] f : flights) {
            Flight flight = new Flight();
            flight.setFlightNumber((String) f[0]);
            flight.setAirline(airlines.get((String) f[1]));
            flight.setOrigin(airports.get((String) f[2]));
            flight.setDestination(airports.get((String) f[3]));
            flight.setDurationMinutes((Integer) f[5]);
            flight.setAircraft(planes[(Integer) f[7]]);
            flight = flightRepository.save(flight);

            LocalTime time = LocalTime.parse((String) f[4]);
            for (int day = 1; day <= 7; day++) {
                scheduleService.create(flight, today.plusDays(day).atTime(time),
                        BigDecimal.valueOf(((Integer) f[6]).longValue()));
            }
        }
    }

    private Aircraft aircraft(String model, int rows, int perRow) {
        Aircraft a = new Aircraft();
        a.setModel(model);
        a.setTotalRows(rows);
        a.setSeatsPerRow(perRow);
        return aircraftRepository.save(a);
    }
}
