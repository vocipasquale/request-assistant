package it.requestassistant.adapters.persistence.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public record MessageEntity(
        String subject,
        String senderAddress,
        LocalDateTime receivedAt,
        String to,
        String cc,
        String bodyText,
        String entryId,
        String conversationId,
        String conversationTopic,
        Integer importance,
        Boolean hasAttachment,
        String category
){

    public MessageEntity{
        Objects.requireNonNull(subject);
        Objects.requireNonNull(senderAddress);
        Objects.requireNonNull(receivedAt);
        Objects.requireNonNull(to);
    }

    @Override
    public String subject() {
        return subject;
    }

    @Override
    public String senderAddress() {
        return senderAddress;
    }

    @Override
    public LocalDateTime receivedAt() {
        return receivedAt;
    }

    @Override
    public String to() {
        return to;
    }

    @Override
    public String cc() {
        return cc;
    }

    @Override
    public String bodyText() {
        return bodyText;
    }

    @Override
    public String entryId() {
        return entryId;
    }

    @Override
    public String conversationId() {
        return conversationId;
    }

    @Override
    public String conversationTopic() {
        return conversationTopic;
    }

    @Override
    public Integer importance() {
        return importance;
    }

    @Override
    public Boolean hasAttachment() {
        return hasAttachment;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public String toString() {
        return "Message{" +
                "subject='" + subject + '\'' +
                ", senderAddress='" + senderAddress + '\'' +
                ", receivedAt=" + receivedAt +
                ", to='" + to + '\'' +
                ", cc='" + cc + '\'' +
                ", bodyText='" + bodyText + '\'' +
                ", entryId='" + entryId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", conversationTopic='" + conversationTopic + '\'' +
                ", importance=" + importance +
                ", hasAttachment=" + hasAttachment +
                ", category='" + category + '\'' +
                '}';
    }
}
