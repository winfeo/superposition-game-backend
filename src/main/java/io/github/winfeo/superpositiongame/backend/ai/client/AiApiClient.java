package io.github.winfeo.superpositiongame.backend.ai.client;

import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionResponseDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiApiClient {
    private final RestClient restClient;

    public AiApiClient(@Qualifier("aiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public AiChooseActionResponseDTO chooseAction(AiChooseActionRequestDTO request) {
        return restClient.post()
                .uri("/choose-action")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiChooseActionResponseDTO.class);
    }
}
