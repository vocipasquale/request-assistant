package it.requestassistant.application.util;

import it.requestassistant.domain.model.Message;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageHelperTest {

    @Test
    void testExtractTkWithValidSubject() {
        String subject = "R: Risoluzione della Richiesta di Supporto [13477581B]";
        Message message = buildMessage(subject);
        String result = MessageHelper.extractTk(message);
        assertEquals("13477581B", result);
    }

    @Test
    void testExtractTkWithNoBrackets() {
        String subject = "R: Risoluzione della Richiesta di Supporto 13477581B";
        Message message = buildMessage(subject);
        String result = MessageHelper.extractTk(message);
        assertNull(result);
    }

    @Test
    void testExtractTkWithOnlyOpeningBracket() {
        String subject = "R: Risoluzione della Richiesta di Supporto [13477581B";
        Message message = buildMessage(subject);
        String result = MessageHelper.extractTk(message);
        assertNull(result);
    }

    @Test
    void testExtractTkWithOnlyClosingBracket() {
        String subject = "R: Risoluzione della Richiesta di Supporto 13477581B]";
        Message message = buildMessage(subject);
        String result = MessageHelper.extractTk(message);
        assertNull(result);
    }

    @Test
    void testExtractTkWithEmptyBrackets() {
        String subject = "R: Risoluzione della Richiesta di Supporto []";
        Message message = buildMessage(subject);
        String result = MessageHelper.extractTk(message);
        assertEquals("", result);
    }

    private Message buildMessage(String subject){
        String senderAddress = "";
        LocalDateTime receivedAt = LocalDateTime.now();
        String to = "";
        String cc = "";
        String bodyText = "";
        String entryId = "";
        String conversationId = "";
        String conversationTopic = "";
        Integer importance = 1;
        Boolean hasAttachment = true;
        String category = "";

        return new Message(subject, senderAddress, receivedAt, to, cc,
                bodyText, entryId, conversationId, conversationTopic,
                importance, hasAttachment, category);
    }
}
