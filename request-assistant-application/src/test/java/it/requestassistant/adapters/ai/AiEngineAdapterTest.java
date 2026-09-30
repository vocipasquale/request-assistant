package it.requestassistant.adapters.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.service.JsonService;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestAI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiEngineAdapterTest {

    @Mock
    private PersistenceDaoPort persistenceDaoPort;

    @Mock
    private JsonService jsonService;

    @Mock
    private AiEngineAdapter aiEngineAdapter;
    private Method generateDecisionOptionsMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        generateDecisionOptionsMethod = AiEngineAdapter.class
                .getDeclaredMethod("generateDecisionOptions", Message.class, Request.class);
        generateDecisionOptionsMethod.setAccessible(true);
    }

    @Test
    void generateDecisionOptionsShouldReturnEmptyListWhenSerializationSucceedsAndCandidateExists() throws Exception {
        Message message = buildMessage();
        Request candidate = buildRequest();
        when(jsonService.toJson(any(RequestAI.class))).thenReturn("{\"result\":\"ok\"}");

        List<DecisionOption> options = invokeGenerateDecisionOptions(message, candidate);

        assertNotNull(options);
        assertTrue(options.isEmpty());

        ArgumentCaptor<RequestAI> requestCaptor = ArgumentCaptor.forClass(RequestAI.class);
        verify(jsonService).toJson(requestCaptor.capture());
        assertSame(message, requestCaptor.getValue().getMessage());
        assertSame(candidate, requestCaptor.getValue().getRequest());
    }

    @Test
    void generateDecisionOptionsShouldReturnEmptyListWhenSerializationSucceedsAndCandidateIsNull() throws Exception {
        Message message = buildMessage();
        when(jsonService.toJson(any(RequestAI.class))).thenReturn("{\"result\":\"ok\"}");

        List<DecisionOption> options = invokeGenerateDecisionOptions(message, null);

        assertNotNull(options);
        assertTrue(options.isEmpty());

        ArgumentCaptor<RequestAI> requestCaptor = ArgumentCaptor.forClass(RequestAI.class);
        verify(jsonService).toJson(requestCaptor.capture());
        assertSame(message, requestCaptor.getValue().getMessage());
        assertNull(requestCaptor.getValue().getRequest());
    }

    @Test
    void generateDecisionOptionsShouldWrapJsonProcessingException() throws Exception {
        Message message = buildMessage();
        Request candidate = buildRequest();
        JsonProcessingException jsonException = new JsonProcessingException("boom") { };
        when(jsonService.toJson(any(RequestAI.class))).thenThrow(jsonException);

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> generateDecisionOptionsMethod.invoke(aiEngineAdapter, message, candidate)
        );

        assertInstanceOf(RuntimeException.class, thrown.getCause());
        assertSame(jsonException, thrown.getCause().getCause());
    }

    @SuppressWarnings("unchecked")
    private List<DecisionOption> invokeGenerateDecisionOptions(Message message, Request candidate) throws Exception {
        return (List<DecisionOption>) generateDecisionOptionsMethod.invoke(aiEngineAdapter, message, candidate);
    }

    private Message buildMessage() {
        return new Message(
                10L,
                "Oggetto test",
                "sender@test.it",
                LocalDateTime.of(2026, 9, 11, 10, 30),
                "to@test.it",
                "cc@test.it",
                "Body di test",
                "ENTRY-123",
                "CONV-123",
                "Topic test",
                1,
                false,
                "INBOX"
        );
    }

    private Request buildRequest() {
        Request request = new Request();
        request.setId(99L);
        request.setTitle("Richiesta di test");
        request.setStatus(Request.Status.IN_PROGRESS);
        request.setCreateAt(LocalDateTime.of(2026, 9, 10, 8, 0));
        request.setUpdateAt(LocalDateTime.of(2026, 9, 11, 9, 0));
        return request;
    }
}

