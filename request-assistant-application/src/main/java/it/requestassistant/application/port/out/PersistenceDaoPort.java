package it.requestassistant.application.port.out;


import it.requestassistant.adapters.persistence.mapper.PersistenceDomainMappers;
import it.requestassistant.adapters.persistence.row.RequestRow;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;

public interface PersistenceDaoPort {
    Request findRequestByConversationId(String s);
    Request findRequestByTk(String tk);
    void insertMessage(Message message);
    void insertRequest(Request request);
    void insertRequestItem(RequestItem requestItem);
    void insertUserAccount(User user);
}
