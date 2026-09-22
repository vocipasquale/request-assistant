package it.requestassistant.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.requestassistant.domain.model.Action;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestAI;
import it.requestassistant.domain.model.ResponseAI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonServiceTest {

    private JsonService jsonService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        jsonService = new JsonService(objectMapper);
    }

    @Test
    void toJsonShouldSerializeRequestAIWithMessageAndRequest() throws JsonProcessingException {
        Message message = buildMessage();
        Request request = buildRequest();
        RequestAI requestAI = new RequestAI(message, request);

        String json = jsonService.toJson(requestAI);

        assertNotNull(json);
        assertAll(
                () -> assertTrue(json.contains("\"message\"")),
                () -> assertTrue(json.contains("\"request\"")),
                () -> assertTrue(json.contains("\"subject\":\"Oggetto test\"")),
                () -> assertTrue(json.contains("\"entryId\":\"ENTRY-123\"")),
                () -> assertTrue(json.contains("\"title\":\"Richiesta di test\"")),
                () -> assertTrue(json.contains("\"status\":\"IN_PROGRESS\""))
        );
    }

    @Test
    void fromJsonShouldDeserializeResponseAIWithDecisionOptions() throws JsonProcessingException {
        String json = """
                {
                  "options": [
                    {
                      "id": 7,
                      "action": {
                        "id": 3,
                        "title": "NUOVA_RICHIESTA"
                      },
                      "confidence": 92.5,
                      "reasons": "Nessuna richiesta compatibile trovata"
                    }
                  ]
                }
                """;

        ResponseAI responseAI = jsonService.fromJson(json);

        assertNotNull(responseAI);
        assertNotNull(responseAI.getOptions());
        assertEquals(1, responseAI.getOptions().size());
        assertAll(
                () -> assertEquals(7L, responseAI.getOptions().getFirst().getId()),
                () -> assertEquals(Action.Title.NUOVA_RICHIESTA, responseAI.getOptions().getFirst().getAction().getTitle()),
                () -> assertEquals(3L, responseAI.getOptions().getFirst().getAction().getId()),
                () -> assertEquals(92.5, responseAI.getOptions().getFirst().getConfidence()),
                () -> assertEquals("Nessuna richiesta compatibile trovata", responseAI.getOptions().getFirst().getReasons())
        );
    }

    @Test
    void fromJsonShouldThrowJsonProcessingExceptionForMalformedJson() {
        String malformedJson = "{\"options\":[{";

        assertThrows(JsonProcessingException.class, () -> jsonService.fromJson(malformedJson));
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
        request.setMessages(List.of(buildMessage()));
        return request;
    }
}

