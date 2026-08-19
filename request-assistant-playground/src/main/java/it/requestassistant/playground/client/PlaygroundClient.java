package it.requestassistant.playground.client;

import it.requestassistant.playground.dto.MessageDto;
import it.requestassistant.playground.dto.MoveInProgressRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PlaygroundClient implements RequestAssistantApplicationRemote{

    private final RestClient client;

    public PlaygroundClient(RestClient client) {
        this.client = client;
    }

    @Override
    public List<MessageDto> getMessagesToProcess() {
        return client
                .get()
                .uri("api/v1/messages")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public void moveMessageInProgress(MoveInProgressRequest request) {
        client
                .post()
                .uri("api/v1/move/inprogress")
                .body(request);
    }

}
