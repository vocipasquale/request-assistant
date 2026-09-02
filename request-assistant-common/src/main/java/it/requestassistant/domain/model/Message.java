package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message{
    private long id;
    private String subject;
    private String senderAddress;
    private LocalDateTime receivedAt;
    private String to;
    private String cc;
    private String bodyText;
    private String entryId;
    private String conversationId;
    private String conversationTopic;
    private Integer importance;
    private Boolean hasAttachment;
    private String category;

    public Message(){}

    public Message(String subject, String senderAddress, LocalDateTime receivedAt, String to, String cc, String bodyText, String entryId, String conversationId, String conversationTopic, Integer importance, Boolean hasAttachment, String category) {
        this.subject = subject;
        this.senderAddress = senderAddress;
        this.receivedAt = receivedAt;
        this.to = to;
        this.cc = cc;
        this.bodyText = bodyText;
        this.entryId = entryId;
        this.conversationId = conversationId;
        this.conversationTopic = conversationTopic;
        this.importance = importance;
        this.hasAttachment = hasAttachment;
        this.category = category;
    }

    public Message(long id, String subject, String senderAddress, LocalDateTime receivedAt, String to, String cc, String bodyText, String entryId, String conversationId, String conversationTopic, Integer importance, Boolean hasAttachment, String category) {
        this.id = id;
        this.subject = subject;
        this.senderAddress = senderAddress;
        this.receivedAt = receivedAt;
        this.to = to;
        this.cc = cc;
        this.bodyText = bodyText;
        this.entryId = entryId;
        this.conversationId = conversationId;
        this.conversationTopic = conversationTopic;
        this.importance = importance;
        this.hasAttachment = hasAttachment;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationTopic() {
        return conversationTopic;
    }

    public void setConversationTopic(String conversationTopic) {
        this.conversationTopic = conversationTopic;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public Boolean getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(Boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
