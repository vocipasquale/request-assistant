package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PendingDecision{
    private long id;
    private LocalDateTime createdAt;
    private Type type;
    private String target;
    private List<DecisionOption> options;
    private Message message;
    private Request request;

    public PendingDecision(long id, LocalDateTime createdAt, Type type, String target, List<DecisionOption> options, Message message, Request request) {
        this.id = id;
        this.createdAt = createdAt;
        this.type = type;
        this.target = target;
        this.options = options;
        this.message = message;
        this.request = request;
    }

    public enum Type {
        MESSAGE_CLASSIFICATION,
        REQUEST_ANALYSIS
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<DecisionOption> getOptions() {
        return options;
    }

    public void setOptions(List<DecisionOption> options) {
        this.options = options;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
