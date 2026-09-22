package it.requestassistant.adapters.batch;

import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.application.port.out.AiEnginePort;
import it.requestassistant.application.port.out.MessagePort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.port.out.RequestResearchPort;
import it.requestassistant.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class BatchPortAdapter implements BatchPort {

    private static final int NUM_GG_SOLLECITO = 2;
    private static final String ITEMS_DA_RICHIEDERE = "ITEM_DA_RICHIEDERE";
    private static final String ITEMS_DA_SOLLECITARE_FP = "SOLLECITO_FP";
    private static final String ITEMS_DA_SOLLECITARE_UTENTE = "SOLLECITO_UTENTE";
    private static final String REQUEST_DA_CHIUDERE = "REQUEST_DA_CHIUDERE";
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessagePort messagePort;

    @Autowired
    private RequestResearchPort requestResearchPort;

    @Autowired
    private AiEnginePort aiAnalyzerPort;

    @Autowired
    private PersistenceDaoPort persistenceDaoPort;

    @Value("${email-address.focal-point}")
    private String focalPointEmailAddress;

    @Value("${email-address.operatore}")
    private String operatoreEmailAddress;

    @Override
    public List<Message> getMessagesToProcess() {
        return messagePort.findMessagesToProcess();
    }



    @Override
    public void moveMessageInProgress(Message message) throws Exception {
        Message movedMessage = messagePort.moveMessageInProgress(message);
        logger.debug("Aggiorno entryId, precedente: {}", message.getEntryId());
        logger.debug("Aggiorno entryId, nuovo: {}", movedMessage.getEntryId());
        persistenceDaoPort.refreshEntryIdMessage(message.getEntryId(), movedMessage.getEntryId());
    }

    @Override
    public boolean processMessage(Message message) {
        logger.debug("Ricerca request per entryId del messaggio {}", message.getEntryId());
        Request request = requestResearchPort.searchByMessage(message);

        //sottopongo il risultato della ricerca all'AI
        //AI si preoccupa di creare le proposte/decisioni che dovrà prendere l'operatore
        logger.debug("Analisi del risultato tramite motore AI...");
        PendingDecision pendingDecision = aiAnalyzerPort.analyzeMessage(message, request);

        //persisto su database
        logger.debug("Persistenza completa su database.");
        persistenceDaoPort.insertPendingDecision(pendingDecision);

        return true;
    }

    @Override
    public List<Request> getRequestsToProcess() {
        return persistenceDaoPort.findRequestsToProcess();
    }

    /**
     * Crea la PendingDecision relativa alla request in input.
     * L'applicazione crea la PendingDecision per:
     * - items da sollecitare al focal point
     * - items da sollecitare all'utente per riscontro
     *
     * Se gli items sono tutti in RISCONTRO_OK, la request viene chiusa
     * La request verrà analizzata dall'assistente AI, solo in presenza di un nuovo message (ved. processMessage(Message message) )
     *
     * @param request
     */
    @Override
    public void processRequest(Request request) {
        logger.info("Gestione richiesta IN_PROGRESS: {}", request.getTitle());

        PendingDecision pendingDecision = null;
        Map<String, List<RequestItem>> situazioneItems = situazioneItems(request);

        switch (situazioneItems.keySet().stream().findFirst().orElse("")) {
            case ITEMS_DA_RICHIEDERE -> {

            }
            case ITEMS_DA_SOLLECITARE_FP -> {
                //sollecito focal point...
                List<RequestItem> items = situazioneItems.get(ITEMS_DA_SOLLECITARE_FP);
                logger.debug("Richiesta id {} creazione sollecito per focal point...", request.getId());
                pendingDecision = creaSollecito(request, items, focalPointEmailAddress);
                long pdId = persistenceDaoPort.insertPendingDecision(pendingDecision);
                logger.info("Creata PendingDecision id {} per sollecito focal point", pdId);
            }
            case ITEMS_DA_SOLLECITARE_UTENTE -> {
                //sollecito utente...
                List<RequestItem> items = situazioneItems.get(ITEMS_DA_SOLLECITARE_UTENTE);
                logger.debug("Richiesta id {} creazione sollecito per utente...", request.getId());
                pendingDecision = creaSollecito(request, items, getEmailAddressUser(request.getUser()));
                long pdId = persistenceDaoPort.insertPendingDecision(pendingDecision);
                logger.info("Creata PendingDecision id {} per sollecito utente", pdId);
            }
            case REQUEST_DA_CHIUDERE -> {
                //request da chiudere...
                logger.debug("Richiesta id {} da chiudere, tutti gli items sono in stato RISCONTRO_OK", request.getId());
                request.setUpdateAt(LocalDateTime.now());
                request.setStatus(Request.Status.DONE);
                request.setNote(request.getNote() + "\nTutti gli items sono in stato RISCONTRO_OK");
                persistenceDaoPort.updateRequest(request);
                logger.info("Chiusa request con id {} perchè tutti gli item erano chiusi.", request.getId());
            }
        }
    }

    private String getEmailAddressUser(User user) {
        return Objects.isNull(user.getEmail())
                ? user.getCognome()+" "+user.getNome()+";"
                : user.getEmail();
    }

    /**
     * restituisce una mappa con chiave il tipo di sollecito e valore la lista di items da sollecitare
     * @param request
     * @return
     */
    private Map<String, List<RequestItem>> situazioneItems(Request request){
        List<RequestItem> itemsSollecitoFP = new ArrayList<>();
        List<RequestItem> itemsSollecitoUtente = new ArrayList<>();
        List<RequestItem> itemsDaChiudere = new ArrayList<>();
        List<RequestItem> itemsDaChiedere = new ArrayList<>();

        request.getItems().forEach(item -> {
            if(List.of(RequestItem.Status.RICHIESTO, RequestItem.Status.SOLLECITATO).contains(item.getStatus())){
                itemsSollecitoFP.add(item);
            }

            if(List.of(RequestItem.Status.IN_ATTESA_RISCONTRO_UTENTE, RequestItem.Status.SOLLECITATO_RISCONTRO_UTENTE).contains(item.getStatus())){
                itemsSollecitoUtente.add(item);
            }

            if(RequestItem.Status.DA_RICHIEDERE.equals(item.getStatus())){
                itemsDaChiedere.add(item);
            }

            if(RequestItem.Status.RISCONTRO_OK.equals(item.getStatus())){
                itemsDaChiudere.add(item);
            }

        });

        if(!itemsDaChiedere.isEmpty()) {
            return Map.of(ITEMS_DA_RICHIEDERE, itemsDaChiedere);
        }

        if(!itemsSollecitoFP.isEmpty()){
            return Map.of(ITEMS_DA_SOLLECITARE_FP, itemsSollecitoFP);
        }

        if(!itemsSollecitoUtente.isEmpty()) {
            return Map.of(ITEMS_DA_SOLLECITARE_UTENTE, itemsSollecitoUtente);
        }

        if(itemsDaChiudere.size() == request.getItems().size()){
            return Map.of(REQUEST_DA_CHIUDERE, itemsDaChiudere);
        }

        return Map.of();

    }


    /**
     * Crea la pending decision di sollecto che dovrà essere valutata e accettata dall'operatore.
     *
     * @param request
     * @param items
     * @param emailAddress
     * @return
     */
    private PendingDecision creaSollecito(Request request, List<RequestItem> items, String emailAddress) {
        //mail di sollecito da inviare
        Message message = buildMessageSollecito(request, items, emailAddress);

        PendingDecision result = new PendingDecision(
                    0,
                    LocalDateTime.now(),
                    PendingDecision.Type.REQUEST_ANALYSIS,
                "SOLLECITO",
                    buildDecisionOptionSollecito(request),
                message,
                request);

            return result;
    }

    /**
     * Crea un messaggio di sollecito da inviare al focal point o all'utente.
     * Il testo deve andar bene sia per l'utente che per il focal point.
     *
     * @param request
     * @param items
     * @return
     */
    private Message buildMessageSollecito(Request request, List<RequestItem> items, String emailAddress) {
        Message message = new Message();
        message.setSenderAddress(operatoreEmailAddress);
        message.setTo(emailAddress);
        message.setSubject("Richiesta riscontro per " + request.getTitle());
        String testo = """
                Buongiorno,
                a meno di mia svista non ho ricevuto riscontro in merito a:\n\n
                """;
        //elenco items...
        items.forEach(i -> testo.concat("\n" + i));
        testo.concat("\n\nGrazie\nSaluti.");
        message.setBodyText(testo);
        message.setImportance(1);
        message.setHasAttachment(false);

        return message;
    }


    private List<DecisionOption> buildDecisionOptionSollecito(Request request) {
        DecisionOption decisionOption = new DecisionOption();
        decisionOption.setConfidence(1.0);
        decisionOption.setReasons("Sono trascorsi almeno "+NUM_GG_SOLLECITO+" dall'ultimo riscontro.");
        decisionOption.setAction(new Action(Action.Title.INVIA_SOLLECITO, "Invia sollecito per " + request.getTitle()));
        List<DecisionOption> options = new ArrayList<>();
        options.add(decisionOption);
        return options;
    }



}
