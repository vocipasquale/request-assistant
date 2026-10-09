package it.requestassistant.domain.model;

import java.util.List;

public class CostruttoreContentRequest extends ClassificatoreContentRequest {
    private String action;

    public CostruttoreContentRequest(String action, Message message, Request request, List<String> attachmentContext) {
        super(message, request, attachmentContext);
        this.action = action;
    }
}
