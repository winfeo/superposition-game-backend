package io.github.winfeo.superpositiongame.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_KEY", dotenv.get("JWT_KEY"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

        SpringApplication.run(Application.class, args);
	}

}
