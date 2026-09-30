package it.requestassistant.adapters.ai;

import java.util.List;

public record OllamaRequest(
        String model,
        List<OllamaMessage> messages,
        Object format,
        boolean stream
) {
}
