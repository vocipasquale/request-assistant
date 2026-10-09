package it.requestassistant.adapters.ai;

import java.util.List;

public record OllamaMessage(
        String role,
        String content, //JSON serializzato che rappresenta la struttura JAVA di input
        List<String> images
) {
}
