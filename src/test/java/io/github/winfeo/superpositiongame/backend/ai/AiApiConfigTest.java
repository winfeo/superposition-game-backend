package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.ai.config.AiApiConfig;
import io.github.winfeo.superpositiongame.backend.ai.config.AiApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AiApiConfigTest {
    @Test
    void createsDedicatedRestClient() {
        AiApiProperties properties = new AiApiProperties(
                URI.create("http://ai.test"),
                Duration.ofSeconds(2),
                Duration.ofSeconds(10)
        );

        RestClient restClient = new AiApiConfig().aiRestClient(properties);

        assertThat(restClient).isNotNull();
    }

    @Test
    void createsDedicatedAiThreadPool() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor)
                new AiApiConfig().aiTaskExecutor();

        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(2);
            assertThat(executor.getMaxPoolSize()).isEqualTo(8);
            assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity())
                    .isEqualTo(100);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("ai-turn-");
        } finally {
            executor.shutdown();
        }
    }
}
