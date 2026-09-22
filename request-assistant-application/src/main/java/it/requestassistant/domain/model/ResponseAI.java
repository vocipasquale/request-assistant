package it.requestassistant.domain.model;

import java.util.List;

public class ResponseAI {
    private List<DecisionOption> options;

    public List<DecisionOption> getOptions() {
        return options;
    }
    public void setOptions(List<DecisionOption> options) {
        this.options = options;
    }


}
