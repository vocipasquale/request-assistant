package it.requestassistant.adapters.dashboard;

import it.requestassistant.application.port.in.DashboardPort;
import it.requestassistant.application.port.out.PlaygroundProcessControlPort;
import org.springframework.stereotype.Component;

@Component
public class DashboardPortadapter implements DashboardPort {

	private final PlaygroundProcessControlPort playgroundProcessControlPort;

	public DashboardPortadapter(PlaygroundProcessControlPort playgroundProcessControlPort) {
		this.playgroundProcessControlPort = playgroundProcessControlPort;
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
}
