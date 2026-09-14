package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.ai.client.AiApiClient;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiApiClientTest {
    @Test
    void sendsChooseActionRequestAndReadsResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiApiClient client = new AiApiClient(builder.build());

        server.expect(requestTo("http://ai.test/choose-action"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model", is("mcts")))
                .andExpect(jsonPath("$.aiPlayerId", is(AI_ID)))
                .andExpect(jsonPath("$.opponentPlayerId", is(HUMAN_ID)))
                .andExpect(jsonPath("$['protected']['ai-player'][0]", is(true)))
                .andRespond(withSuccess("""
                        {
                          "model": "mcts",
                          "action": {
                            "type": "SURRENDER",
                            "playerId": "ai-player"
                          },
                          "thinkingTimeMs": 15.5,
                          "iterations": 120,
                          "rootChildren": 6,
                          "neuralModel": null
                        }
                        """, MediaType.APPLICATION_JSON));

        AiChooseActionResponseDTO response = client.chooseAction(request());

        assertThat(response.model()).isEqualTo("mcts");
        assertThat(response.action().get("type").asText()).isEqualTo("SURRENDER");
        assertThat(response.thinkingTimeMs()).isEqualTo(15.5);
        assertThat(response.iterations()).isEqualTo(120);
        server.verify();
    }

    @Test
    void propagatesHttpErrorToTurnService() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiApiClient client = new AiApiClient(builder.build());

        server.expect(requestTo("http://ai.test/choose-action"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.chooseAction(request()))
                .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }

    private AiChooseActionRequestDTO request() {
        return new AiChooseActionRequestDTO(
                "mcts",
                AI_ID,
                HUMAN_ID,
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of(AI_ID, List.of(true, false, false, false)),
                1,
                80,
                1,
                AI_ID,
                false
        );
    }
}
