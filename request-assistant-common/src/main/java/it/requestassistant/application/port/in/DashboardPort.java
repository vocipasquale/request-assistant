package it.requestassistant.application.port.in;

import it.requestassistant.domain.model.PendingDecision;

import java.util.List;

public interface DashboardPort {
	void startPlaygroundProcess();
	void stopPlaygroundProcess();
	boolean isPlaygroundProcessRunning();
	List<PendingDecision> getMessagePendingDecisions();
	void deletePendingDecision(long id);
	void deleteDecisionOption(long id);
	void acceptPendingDecision(long id);
}
