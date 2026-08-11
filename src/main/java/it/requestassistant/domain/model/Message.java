package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Message(
        String sender,
        List<String> reciversTo,
        List<String> reciversCc,
        String subject,
        String bodyText,
        String bodyHtml,
        LocalDateTime receivedAt){

    public Message{
        Objects.requireNonNull(sender);
        Objects.requireNonNull(reciversTo);
        Objects.requireNonNull(reciversCc);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(bodyText);
        Objects.requireNonNull(bodyHtml);
        Objects.requireNonNull(receivedAt);
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
