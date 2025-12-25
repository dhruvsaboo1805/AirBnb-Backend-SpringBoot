package com.example.AirbnbBookingSpring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AirbnbBookingSpringApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
//        // Debug: Print loaded values (remove passwords in production!)
//        System.out.println("=== Loading .env values ===");
//        System.out.println("DATABASE_URL: " + dotenv.get("DATABASE_URL"));
//        System.out.println("DATABASE_USERNAME: " + dotenv.get("DATABASE_USERNAME"));
//        System.out.println("DATABASE_PASSWORD: " + (dotenv.get("DATABASE_PASSWORD") != null ? dotenv.get("DATABASE_PASSWORD") : "NOT FOUND"));
//        System.out.println("===========================");

        // Set system properties from .env
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
		SpringApplication.run(AirbnbBookingSpringApplication.class, args);
	}

}
