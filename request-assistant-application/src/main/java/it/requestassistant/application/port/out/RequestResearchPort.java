package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;

import java.util.List;

public interface RequestResearchPort {
    Request searchByMessage(Message message) ;
}
