package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class MailMessage {

    private final String sender;
    private final String subject;
    private final String bodyText;
    private final String bodyHtml;
    private final LocalDateTime receivedAt;

    public MailMessage(
            String sender,
            String subject,
            String bodyText,
            String bodyHtml,
            LocalDateTime receivedAt) {

        this.sender = Objects.requireNonNull(sender);
        this.subject = Objects.requireNonNull(subject);
        this.bodyText = bodyText;
        this.bodyHtml = bodyHtml;
        this.receivedAt = Objects.requireNonNull(receivedAt);
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getBodyText() {
        return bodyText;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "sender='" + sender + '\'' +
                ", subject='" + subject + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}