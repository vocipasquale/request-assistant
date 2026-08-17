package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;

import java.util.List;

public interface RequestSearch {
    //la ricerca deve restituire o una Request o nessuna
    Request search(Message message);
}
