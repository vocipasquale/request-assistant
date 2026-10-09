package it.requestassistant.adapters.ai;

public record OllamaOptions(
        double temperature,
        double top_k,
        double top_p
) {
}
