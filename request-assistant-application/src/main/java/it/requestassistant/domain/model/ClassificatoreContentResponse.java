package it.requestassistant.domain.model;

public class ClassificatoreContentResponse {
    private String action;
    private int confidence;
    private String reasons;

    public ClassificatoreContentResponse(String action, int confidence, String reasons) {
        this.action = action;
        this.confidence = confidence;
        this.reasons = reasons;
    }
}
