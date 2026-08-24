package it.requestassistant.application.port.in;

public interface DashboardPort {

	void startPlaygroundProcess();

	void stopPlaygroundProcess();

	boolean isPlaygroundProcessRunning();
}
