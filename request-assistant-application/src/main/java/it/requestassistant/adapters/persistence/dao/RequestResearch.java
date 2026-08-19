package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.application.port.out.RequestResearchPort;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestResearch implements RequestResearchPort {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Request searchByMessage(Message message) {
        logger.debug("searchByMessage....");
        return null;
    }

}
