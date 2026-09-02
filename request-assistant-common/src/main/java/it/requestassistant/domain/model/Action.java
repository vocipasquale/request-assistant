package it.requestassistant.domain.model;

import java.util.List;
import java.util.Objects;

public class Action{
    private long id;
    private String title;
    private List<String> steps;

    public Action (){}

    public Action(String title, List<String> steps) {
        this.title = title;
        this.steps = steps;
    }

    public Action(long id, String title, List<String> steps) {
        this.id = id;
        this.title = title;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}
