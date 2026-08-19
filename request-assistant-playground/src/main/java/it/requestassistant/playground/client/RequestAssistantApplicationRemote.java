package it.requestassistant.playground.client;

import it.requestassistant.playground.dto.MessageDto;
import it.requestassistant.playground.dto.MoveInProgressRequest;

import java.util.List;

public interface RequestAssistantApplicationRemote {

    List<MessageDto> getMessagesToProcess();
    void moveMessageInProgress(MoveInProgressRequest request);
}
