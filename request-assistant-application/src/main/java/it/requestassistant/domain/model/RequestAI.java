package it.requestassistant.domain.model;

public class RequestAI {
    private Message message;
    private Request request;

    public RequestAI(Message message, Request request) {
        this.message = message;
        this.request = request;
    }

    public RequestAI(Request request) {
        this.request = request;
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
