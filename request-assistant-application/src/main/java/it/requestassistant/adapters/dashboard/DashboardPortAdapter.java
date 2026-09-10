package it.requestassistant.adapters.dashboard;

import it.requestassistant.application.port.in.DashboardPort;
import it.requestassistant.application.port.out.*;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class DashboardPortAdapter implements DashboardPort {

	private final PlaygroundProcessControlPort playgroundProcessControlPort;
	private final PersistenceDaoPort persistenceDaoPort;
	private final MessagePort messagePort;
	private final ActionPerformerPort actionPerformerPort;

	public DashboardPortAdapter(PlaygroundProcessControlPort playgroundProcessControlPort,
                                PersistenceDaoPort persistenceDaoPort, MessagePort messagePort, ActionPerformerPort actionPerformerPort) {
		this.playgroundProcessControlPort = playgroundProcessControlPort;
		this.persistenceDaoPort = persistenceDaoPort;
        this.messagePort = messagePort;
        this.actionPerformerPort = actionPerformerPort;
    }

	@Override
	public void startPlaygroundProcess() {
		playgroundProcessControlPort.start();
	}

	@Override
	public void stopPlaygroundProcess() {
		playgroundProcessControlPort.stop();
	}

	@Override
	public boolean isPlaygroundProcessRunning() {
		return playgroundProcessControlPort.isRunning();
	}

	@Override
	public List<PendingDecision> getMessagePendingDecisions() {
		return persistenceDaoPort.findPendingDecisionsByType(PendingDecision.Type.MESSAGE_CLASSIFICATION);
	}

	@Override
	public void deletePendingDecision(PendingDecision pd) {
		if(!Objects.isNull(pd.getMessage())){//prima sposta la mail in "scartate"
            try {
                messagePort.moveMessageInDiscarded(pd.getMessage());
				//poi ripulisco il DB
				persistenceDaoPort.deletePendingDecision(pd);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

	}


	@Override
	public void acceptDecisionOption(PendingDecision pendingDecision, DecisionOption decisionOption)  {
        try {
			actionPerformerPort.perform(pendingDecision, decisionOption);
			//dopo aver eseguito le azioni, elimino la pending decision dal DB
			persistenceDaoPort.deletePendingDecision(pendingDecision);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
	}



	@Override
	public void showMessage(Message message) {
		messagePort.displayMessage(message);
	}

	@Override
	public List<PendingDecision> getRequestPendingDecisions() {
		return persistenceDaoPort.findPendingDecisionsByType(PendingDecision.Type.REQUEST_ANALYSIS);
	}
}

