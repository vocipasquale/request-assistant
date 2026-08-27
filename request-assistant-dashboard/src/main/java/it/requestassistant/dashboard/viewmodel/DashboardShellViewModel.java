package it.requestassistant.dashboard.viewmodel;

import it.requestassistant.application.port.in.DashboardPort;
import it.requestassistant.domain.model.PendingDecision;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DashboardShellViewModel {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DashboardPort dashboardPort;
    private final ObjectProperty<DashboardSection> selectedSection = new SimpleObjectProperty<>(DashboardSection.IN_PROGRESS);
    private final BooleanProperty batchRunning = new SimpleBooleanProperty(false);
    private final StringProperty batchStatusText = new SimpleStringProperty("Batch: STOPPED");
    private final StringProperty batchToggleText = new SimpleStringProperty("Avvia batch");

    public DashboardShellViewModel(DashboardPort dashboardPort) {
        this.dashboardPort = dashboardPort;
    }

    @PostConstruct
    void init() {
        refreshBatchState();
    }

    @PreDestroy
    void shutdown() {
        logger.debug("#########################  shutdown");
        dashboardPort.stopPlaygroundProcess();
    }


    public void selectSection(DashboardSection section) {
        logger.debug("Selezionata sezione: " + section);
        selectedSection.set(section);
    }


    public void toggleBatch() {
        if (batchRunning.get()) {
            dashboardPort.stopPlaygroundProcess();
        } else {
            dashboardPort.startPlaygroundProcess();
        }
        refreshBatchState();
    }

    public void refreshBatchState() {
        boolean running = dashboardPort.isPlaygroundProcessRunning();
        batchRunning.set(running);
        batchStatusText.set(running ? "Batch: RUNNING" : "Batch: STOPPED");
        batchToggleText.set(running ? "Stop batch" : "Avvia batch");
    }

    public List<PendingDecision> getMessagePendingDecisions() {
        return dashboardPort.getMessagePendingDecisions();
    }

    public void deletePendingDecision(long id) {
        dashboardPort.deletePendingDecision(id);
    }

    public void acceptDecisionOption(long id) {
        dashboardPort.acceptDecisionOption(id);
    }


    public ObjectProperty<DashboardSection> selectedSectionProperty() {
        return selectedSection;
    }

    public BooleanProperty batchRunningProperty() {
        return batchRunning;
    }

    public StringProperty batchStatusTextProperty() {
        return batchStatusText;
    }

    public StringProperty batchToggleTextProperty() {
        return batchToggleText;
    }
}

