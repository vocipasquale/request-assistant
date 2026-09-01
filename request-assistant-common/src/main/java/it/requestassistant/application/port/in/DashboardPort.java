package it.requestassistant.application.port.in;

import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;

import java.util.List;

public interface DashboardPort {
	void startPlaygroundProcess();
	void stopPlaygroundProcess();
	boolean isPlaygroundProcessRunning();
	List<PendingDecision> getMessagePendingDecisions();
	void deletePendingDecision(PendingDecision pd);
	void acceptDecisionOption(PendingDecision pendingDecision, DecisionOption decisionOption);
    void showMessage(Message message);
}
