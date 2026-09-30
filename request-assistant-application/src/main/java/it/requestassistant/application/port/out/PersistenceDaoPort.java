package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.*;

import java.util.List;

public interface PersistenceDaoPort {

    Request findRequestByConversationId(String conversationId);
    Request findRequestByTk(String tk);
    List<PendingDecision> findPendingDecisionsByType(PendingDecision.Type type);
    List<PendingDecision> findAllPendingDecisions();
    long insertRequest(Request request);
    long insertPendingDecision(PendingDecision pendingDecision);
    void deletePendingDecision(PendingDecision pendingDecision);
    void deleteDecisionOption(long id);

    void refreshEntryIdMessage(String s, String string);

    void updateRequest(Request existingRequest);

    List<Request> findRequestsToProcess();

    long insertMessage(Message message);
}
