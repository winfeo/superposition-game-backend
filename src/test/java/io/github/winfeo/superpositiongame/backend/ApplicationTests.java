package io.github.winfeo.superpositiongame.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.profiles.active=local",
		"spring.liquibase.enabled=false",
		"jwt.key=01234567890123456789012345678901",
		"jwt.expiration=3600000",
		"ai.api.base-url=http://localhost:8000"
})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
