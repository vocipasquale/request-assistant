package it.requestassistant.application.util;

import it.requestassistant.domain.model.Message;

public class MessageHelper {

    public static String extractTk(Message message) {
        String subject = message.subject();

        int start = subject.indexOf('[');
        int end = subject.indexOf(']', start);

        if (start == -1 || end == -1) {
            return null;
        }

        return subject.substring(start + 1, end);
    }
}
