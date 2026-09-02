package it.requestassistant.domain.model;

import java.util.Objects;

public class DecisionOption{
    private long id;
    private Action action;
    private double confidence;
    private String reasons;

    public DecisionOption (){}

    public DecisionOption(Action action, double confidence, String reasons) {
        this.action = action;
        this.confidence = confidence;
        this.reasons = reasons;
    }

    public DecisionOption(long id, Action action, double confidence, String reasons) {
        this.id = id;
        this.action = action;
        this.confidence = confidence;
        this.reasons = reasons;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }
}
