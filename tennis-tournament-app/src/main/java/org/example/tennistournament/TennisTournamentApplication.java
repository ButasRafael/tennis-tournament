package org.example.tennistournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TennisTournamentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TennisTournamentApplication.class, args);
    }

}
