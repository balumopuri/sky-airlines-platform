package com.airline.bakend.controller;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.entity.Airline;
import com.airline.bakend.entity.Airport;
import com.airline.bakend.repository.AirlineRepository;
import com.airline.bakend.repository.AirportRepository;
import com.airline.bakend.service.FlightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Public endpoints: anyone can look at airports and search flights.
@RestController
public class CatalogController {

    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;
    private final FlightService flightService;

    public CatalogController(AirportRepository airportRepository, AirlineRepository airlineRepository,
                             FlightService flightService) {
        this.airportRepository = airportRepository;
        this.airlineRepository = airlineRepository;
        this.flightService = flightService;
    }

    @GetMapping("/api/airports")
    public List<Airport> airports() {
        return airportRepository.findAll();
    }

    @GetMapping("/api/airlines")
    public List<Airline> airlines() {
        return airlineRepository.findAll();
    }

    @GetMapping("/api/flights/search")
    public List<ScheduleDto> search(@RequestParam("from") String from,
                                    @RequestParam("to") String to,
                                    @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam(name = "passengers", defaultValue = "1") int passengers) {
        return flightService.search(from, to, date, passengers);
    }

    @GetMapping("/api/schedules/{id}")
    public ScheduleDto schedule(@PathVariable("id") Long id) {
        return flightService.getSchedule(id);
    }

    @GetMapping("/api/schedules/{id}/seats")
    public List<SeatDto> seats(@PathVariable("id") Long id) {
        return flightService.getSeats(id);
    }
}
