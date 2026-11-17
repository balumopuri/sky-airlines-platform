package com.airline.bakend.repository;

import com.airline.bakend.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    boolean existsByCode(String code);
}
