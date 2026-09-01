package it.requestassistant.domain.model;

import java.util.List;
import java.util.Objects;

public record Action(
        String title,
        List<String> steps
) {
    public Action {
        Objects.requireNonNull(title);
        Objects.requireNonNull(steps);
    }
}
