package it.requestassistant.playground;

import it.requestassistant.application.port.out.RequestSearch;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaygroundRequestSearch implements RequestSearch {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * se restituisce null: non è stata trovata corrispondenza con il messaggio
     * se non restituisce null il messaggio ha attinenza con la request trovata (proposta)
     * @param message
     * @return
     */
    @Override
    public Request search(Message message) {

        logger.info("RequestSearch: ricerca Request per Message " + message.entryId());

        // TODO: utilizzare le informazioni del Message
        // per individuare le Request candidate.

        return null;
    }
}
