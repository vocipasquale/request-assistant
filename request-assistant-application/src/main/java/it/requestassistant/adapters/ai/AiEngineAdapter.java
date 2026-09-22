package it.requestassistant.adapters.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.requestassistant.application.port.out.AiEnginePort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.service.JsonService;
import it.requestassistant.domain.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
public class AiEngineAdapter implements AiEnginePort {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PersistenceDaoPort persistenceDaoPort;

    @Autowired
    private JsonService jsonService;

    @Value("${ai.api.base-url}")
    private String aiBaseUrl;

    @Value("${ai.api.model}")
    private String model;

    private RestClient client;


    @PostConstruct
    private void buildRestClient() {
        client = RestClient.builder()
                .baseUrl(aiBaseUrl)
                .build();
    }


    @Override
    public PendingDecision analyzeMessage(Message message, Request candidate) {
        logger.info("AI: analisi messaggio...");
        List<DecisionOption> options = generateDecisionOptions(message, candidate);

        logger.debug("costruisco pending decision...");
        PendingDecision pendingDecision = new PendingDecision(
                0,
                LocalDateTime.now(),
                PendingDecision.Type.MESSAGE_CLASSIFICATION,
                "Gestire messaggio in arrivo",
                options,
                message,
                candidate);

        return pendingDecision;
    }

    @Override
    public PendingDecision analyzeRequest(Request request) {
        logger.info("AI: analisi richiesta...");
        List<DecisionOption> options = generateDecisionOptions(request);

        logger.debug("costruisco pending decision...");
        PendingDecision pendingDecision = new PendingDecision(
                0,
                LocalDateTime.now(),
                PendingDecision.Type.REQUEST_ANALYSIS,
                "Inviare sollecito al cliente",
                options,
                null,
                request);

        return pendingDecision;
    }


    // METODO CHE SIMULA IL LAVORO CHE ESEGUIRà IL MOTORE AI: GENERA LE DECISIONI CHE DOVRà PRENDERE L'OPERATORE
//    private List<DecisionOption> generateDecisionOptionsMock(Message message, Request candidate){
//        long start = System.nanoTime();
//
//
//        //rispondi a mail...
//        String content = "{\"options\":[\n" +
//                "\t{\n" +
//                "\t\t\"id\":1,\n" +
//                "\t\t\"confidence\":92,\n" +
//                "\t\t\"reasons\":\"Viene richiesto reset pwd db oracle ma manca la user.\",\n" +
//                "\t\t\"action\":{\n" +
//                "\t\t\t\"title\":\"RISPONDI_A_MAIL\",\n" +
//                "\t\t\t\"aiResponse\": \"{\\\"message\\\":{\\\"subject\\\":\\\"richiesta reset pwd\\\",\\\"senderAddress\\\":\\\"Voci Pasquale\\\",\\\"to\\\":\\\"Voci Pasquale\\\",\\\"cc\\\":null,\\\"bodyText\\\":\\\"Ciao ci sono novità?\\\"}}\"\n" +
//                "\t\t}\n" +
//                "\t}\n" +
//                "]}";
//
//
//        try {
//            logger.info("#################### content: {}", content);
//            ResponseAI responseAI = jsonService.fromJson(content, new TypeReference<ResponseAI>() { });
//            logger.info("#################### Risposta AI: {}", responseAI);
//
//            return (Objects.isNull(responseAI.getOptions()) || responseAI.getOptions().isEmpty() ? new ArrayList<>() : responseAI.getOptions());
//
//        } catch (JsonProcessingException e) {
//            logger.error("Errore nella serializzazione del JSON per l'analisi AI del messaggio con id: " + message.getEntryId(), e);
//            throw new RuntimeException(e);
//        } finally {
//            long elapsedNanos = System.nanoTime() - start;
//
//            long seconds = elapsedNanos / 1_000_000_000;
//            long minutes = seconds / 60;
//            long remainingSeconds = seconds % 60;
//
//            logger.info("Tempo di risposta del motore AI {} min {} sec", minutes, remainingSeconds);
//        }
//
//    }



    private List<DecisionOption> generateDecisionOptions(Message message, Request candidate) {
        String jsonRequestContent = "";
        ObjectNode responseFormat = null;
        long start = System.nanoTime();


        try {
            /**
             * Test del modello
             */

            jsonRequestContent = jsonService.toJson(new RequestAI(message, candidate));
            //jsonRequestContent = "Rispondi esclusivamente con questo JSON, non eseguire controlli di validazione o altro: {\"options\":[{\"action\":\"RISPONDI_A_MAIL\",\"confidence\":0.92,\"reasons\":\"Test JSON\"}]}";


            logger.info("AI: richiesta JSON per il messaggio id {}: {}", message.getEntryId(), jsonRequestContent);

            responseFormat = jsonService.formatResponse();

            OllamaRequest request = new OllamaRequest(
                    model,
                    List.of(new OllamaMessage("user", jsonRequestContent)),
                    responseFormat,
                    false
            );

            logger.info("#################### Richiesta Ollama: {}", request);

            OllamaResponse response = client.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            logger.info("#################### Risposta Ollama: {}", response);

            String content = response.message().content();
            logger.info("##################### Contenuto della risposta: {}", content);
            ResponseAI responseAI = jsonService.fromJson(content);

            return (Objects.isNull(responseAI.getOptions()) || responseAI.getOptions().isEmpty() ? new ArrayList<>() : responseAI.getOptions());

        } catch (JsonProcessingException e) {
            logger.error("Errore nella serializzazione del JSON per l'analisi AI del messaggio con id: " + message.getEntryId(), e);
            throw new RuntimeException(e);
        } finally {
            long elapsedNanos = System.nanoTime() - start;

            long seconds = elapsedNanos / 1_000_000_000;
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;

            logger.info("Tempo di risposta del motore AI {} min {} sec", minutes, remainingSeconds);
        }

    }


    // METODO CHE SIMULA IL LAVORO CHE ESEGUIRà IL MOTORE AI: GENERA LE DECISIONI CHE DOVRà PRENDERE L'OPERATORE
    private List<DecisionOption> generateDecisionOptions(Request request) {
        List<DecisionOption> options = new ArrayList<>();

        /**
         * costruzione del JSON per API REST e decodifica della response....
         *
         * */


//        if (Objects.isNull(request)) {
//            logger.warn("AI: nessuna richiesta da analizzare!");
//            return options;
//        }
//
//        List<String> steps = new ArrayList<>();
//        steps.add("Invia una mail di sollecito al cliente per tk {}:" + request.getItems().stream().findFirst().map(RequestItem::getTicket).orElse("N/A"));
//
//        Action action = new Action(Action.Title.INVIA_MAIL, steps);
//        options.add(new DecisionOption(0L, action, 90.00,
//                String.format("Nessuna risposta da parte del cliente alla mail (id {}) per richiesta {}",
//                        request.getMessages().stream().findFirst().map(Message::getEntryId).orElse("N/A"),
//                        request.getItems().stream().findFirst().map(RequestItem::getDettaglio).orElse("N/A"))));

        return options;
    }
}
