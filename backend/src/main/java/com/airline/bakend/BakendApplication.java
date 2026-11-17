package com.airline.bakend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BakendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BakendApplication.class, args);
    }
}
