package it.requestassistant.domain.model;

import java.util.List;

public class ClassificatoreContentRequest {
    private Message message;
    private Request request;
    private List<String> attachmentContext;

    public ClassificatoreContentRequest(Message message, Request request, List<String> attachmentContext) {
        this.message = message;
        this.request = request;
        this.attachmentContext = attachmentContext;
    }
}
