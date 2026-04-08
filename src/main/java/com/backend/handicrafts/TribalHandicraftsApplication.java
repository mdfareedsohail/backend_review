package com.backend.handicrafts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TribalHandicraftsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TribalHandicraftsApplication.class, args);
    }
}