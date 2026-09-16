package it.requestassistant.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.requestassistant.domain.model.DataAction;
import it.requestassistant.domain.model.RequestAI;
import it.requestassistant.domain.model.ResponseAI;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class JsonService {

    private final ObjectMapper objectMapper;

    public JsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(RequestAI requestAI) throws JsonProcessingException {
        return objectMapper.writeValueAsString(requestAI);
    }

//    public ResponseAI fromJson(String json) throws JsonProcessingException {
//        TypeReference<ResponseAI> typeReference = new TypeReference<>() {};
//        return objectMapper.readValue(json, typeReference);
//    }

    /**
     * Converte la stringa json in strttura dati di tipo T, utilizzando TypeReference per gestire i tipi generici.
     * Esempio di utilizzo: ResponseAI response = jsonService.fromJson(json, new TypeReference<ResponseAI>() {});
     *
     * @param json
     * @param typeReference
     * @return
     * @throws JsonProcessingException
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(json, typeReference);
    }




    public ObjectNode formatResponse() {
        ObjectNode format = objectMapper.createObjectNode();
        format.put("type", "object");

        ObjectNode rootProperties = format.putObject("properties");
        ObjectNode options = rootProperties.putObject("options");
        options.put("type", "array");

        ObjectNode optionItem = options.putObject("items");
        optionItem.put("type", "object");

        ObjectNode optionProperties = optionItem.putObject("properties");
        optionProperties.putObject("id").put("type", "integer");
        optionProperties.putObject("confidence").put("type", "number");
        optionProperties.putObject("reasons").put("type", "string");

        ObjectNode action = optionProperties.putObject("action");
        action.put("type", "object");

        ObjectNode actionProperties = action.putObject("properties");
//        actionProperties.putObject("id").put("type", "integer");

        ObjectNode title = actionProperties.putObject("title");
        title.put("type", "string");
        ArrayNode titleValues = title.putArray("enum");
        titleValues.add("RISPONDI_A_MAIL");
        titleValues.add("INOLTRA_MAIL");
        titleValues.add("NUOVA_RICHIESTA");
        titleValues.add("MODIFICA_RICHIESTA");
        titleValues.add("CHIUDI_RICHIESTA");

        actionProperties.putObject("aiResponse").put("type", "string");

        ArrayNode actionRequired = action.putArray("required");
        actionRequired.add("title");

        ArrayNode optionRequired = optionItem.putArray("required");
        optionRequired.add("action");
        optionRequired.add("confidence");
        optionRequired.add("reasons");

        ArrayNode rootRequired = format.putArray("required");
        rootRequired.add("options");

        return format;
    }

}
