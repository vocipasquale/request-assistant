package it.requestassistant.batch;

import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaygroundProcessTest {

    @Mock
    private BatchPort batchPort;

    @Test
    void run_doesNothingWhenNoMessages() {
        PlaygroundProcess process = new PlaygroundProcess(batchPort);
        when(batchPort.getMessagesToProcess()).thenReturn(List.of());

        process.runOnce();

        InOrder inOrder = inOrder(batchPort);
        inOrder.verify(batchPort).getMessagesToProcess();
        verifyNoMoreInteractions(batchPort);
    }

    @Test
    void run_invokesAllBatchPortMethodsForEachMessage() throws Exception {
        PlaygroundProcess process = new PlaygroundProcess(batchPort);

        Message firstMessage = buildMessage("entry-1");
        Message secondMessage = buildMessage("entry-2");
        Request firstRequest = new Request();
        Request secondRequest = new Request();
        List<DecisionOption> firstOptions = List.of(new DecisionOption("LINK_TO_REQUEST", 0.90, "subject-match"));
        List<DecisionOption> secondOptions = List.of(new DecisionOption("CREATE_NEW_REQUEST", 0.80, "no-existing-request"));

        when(batchPort.getMessagesToProcess()).thenReturn(List.of(firstMessage, secondMessage));
        when(batchPort.searchRequestForMessage(firstMessage)).thenReturn(firstRequest);
        when(batchPort.searchRequestForMessage(secondMessage)).thenReturn(secondRequest);
        when(batchPort.generateProposal(firstMessage, firstRequest)).thenReturn(firstOptions);
        when(batchPort.generateProposal(secondMessage, secondRequest)).thenReturn(secondOptions);

        process.runOnce();

        InOrder inOrder = inOrder(batchPort);
        inOrder.verify(batchPort).getMessagesToProcess();

        inOrder.verify(batchPort).searchRequestForMessage(firstMessage);
        inOrder.verify(batchPort).persistMessage(firstMessage);
        inOrder.verify(batchPort).generateProposal(firstMessage, firstRequest);
        inOrder.verify(batchPort).generateDecision(firstMessage, firstRequest, firstOptions);
        inOrder.verify(batchPort).moveMessageInProgress(firstMessage);

        inOrder.verify(batchPort).searchRequestForMessage(secondMessage);
        inOrder.verify(batchPort).persistMessage(secondMessage);
        inOrder.verify(batchPort).generateProposal(secondMessage, secondRequest);
        inOrder.verify(batchPort).generateDecision(secondMessage, secondRequest, secondOptions);
        inOrder.verify(batchPort).moveMessageInProgress(secondMessage);

        verifyNoMoreInteractions(batchPort);
    }

    private Message buildMessage(String entryId) {
        return new Message(
                "Oggetto test",
                "sender@test.it",
                LocalDateTime.now(),
                "dest@test.it",
                null,
                "body",
                entryId,
                "conv-id",
                "conv-topic",
                1,
                false,
                "NEW"
        );
    }
}

