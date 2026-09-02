package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.application.util.MessageHelper;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.port.out.RequestResearchPort;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RequestResearch implements RequestResearchPort {

    private final PersistenceDaoPort persistenceDaoPort;


    public Logger logger = LoggerFactory.getLogger(this.getClass());

    public RequestResearch(PersistenceDaoPort persistenceDaoPort) {
        this.persistenceDaoPort = persistenceDaoPort;
    }

    /**
     * Per ora mi limito ad eseguire un ricerca su database
     * ma l'obiettivo finale e di delegare la ricerca al motore AI.
     *
     * 1. ricerca di Request con Message che hanno stesso "ConversationID" del message in input
     * 2. ricerca di Request con RequestItem che hanno tk uguale a quello contenuto nell'oggetto del Message in input
     * 3. ...altro
     *
     * @param message
     * @return
     */
    @Override
    public Request searchByMessage(Message message) {
        logger.debug("searchByMessage....");
        Request result = null;

        logger.info("Ricerca request per coversationId: "+message.getConversationId());
        result = persistenceDaoPort.findRequestByConversationId(message.getConversationId());

        if (Objects.isNull(result)){
            logger.info("La ricerca per coversationId non ha prodotto risultati.");
            String tk = MessageHelper.extractTk(message);
            if (!Objects.isNull(tk)) {
                logger.info("Ricerca request per TK: " + tk);
                result = persistenceDaoPort.findRequestByTk(tk);
            }

            if (Objects.isNull(result)) {
                logger.info("La ricerca per tk non ha prodotto risultati.");
            }
        }

        //...altro...

        return result;
    }

}
