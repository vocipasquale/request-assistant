package it.requestassistant.batch;

import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.domain.model.Message;
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
    void run_doesNothingWhenNoMessages() throws Exception {
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

        when(batchPort.getMessagesToProcess()).thenReturn(List.of(firstMessage, secondMessage));
        when(batchPort.processMessage(firstMessage)).thenReturn(true);
        when(batchPort.processMessage(secondMessage)).thenReturn(false);

        process.runOnce();

        InOrder inOrder = inOrder(batchPort);
        inOrder.verify(batchPort).getMessagesToProcess();
        inOrder.verify(batchPort).moveMessageInProgress(firstMessage);
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

