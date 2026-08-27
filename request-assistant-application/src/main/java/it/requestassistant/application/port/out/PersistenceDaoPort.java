package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.*;

import java.util.List;

public interface PersistenceDaoPort {

    Request findRequestByConversationId(String conversationId);
    Request findRequestByTk(String tk);
    List<PendingDecision> findPendingDecisionsByType(PendingDecision.Type type);
    long insertRequest(Request request);
    long insertPendingDecision(PendingDecision pendingDecision);
    void deletePendingDecision(long id);
    void deleteDecisionOption(long id);
}
