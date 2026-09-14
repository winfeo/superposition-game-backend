package io.github.winfeo.superpositiongame.backend;

import io.github.winfeo.superpositiongame.backend.ai.config.AiApiProperties;
import io.github.winfeo.superpositiongame.backend.config.GamePresenceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        GamePresenceProperties.class,
        AiApiProperties.class
})
public class Application {
	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
	}

}
