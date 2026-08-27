package it.requestassistant.adapters.dashboard;

import it.requestassistant.application.port.in.DashboardPort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.port.out.PlaygroundProcessControlPort;
import it.requestassistant.domain.model.PendingDecision;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DashboardPortadapter implements DashboardPort {

	private final PlaygroundProcessControlPort playgroundProcessControlPort;
	private final PersistenceDaoPort persistenceDaoPort;

	public DashboardPortadapter(PlaygroundProcessControlPort playgroundProcessControlPort,
								PersistenceDaoPort persistenceDaoPort) {
		this.playgroundProcessControlPort = playgroundProcessControlPort;
		this.persistenceDaoPort = persistenceDaoPort;
	}

	// ── Batch control ──────────────────────────────────────────────────────────

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
	public void deletePendingDecision(long id) {
		persistenceDaoPort.deletePendingDecision(id);
	}


	@Override
	public void acceptDecisionOption(long id) {
		// TODO: implementare la logica di applicazione della decisione in base al tipo.
		// Per ora elimina semplicemente la PendingDecision (e le option in cascade via FK).
		persistenceDaoPort.deletePendingDecision(id);
	}
}

