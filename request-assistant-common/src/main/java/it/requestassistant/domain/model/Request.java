package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class Request {
    private long id;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String title;
    private Status status;
    private User user;
    private String note;
    private List<RequestItem> items;
    private List<Message> messages;


    private enum Status {
        IN_PROGRES,
        COMPLETE;
    }
}
