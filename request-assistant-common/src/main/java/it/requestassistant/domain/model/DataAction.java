package it.requestassistant.domain.model;

public record DataAction(
        Message message,
        Request request
) {
    public DataAction(Message message, Request request) {
        this.message = message;
        this.request = request;
    }
}
