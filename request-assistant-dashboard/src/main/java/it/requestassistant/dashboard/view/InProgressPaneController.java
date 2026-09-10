package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.PendingDecision;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("/it/requestassistant/dashboard/view/in-progress-pane.fxml")
public class InProgressPaneController {

    private final DashboardShellViewModel viewModel;
    private final FxWeaver fxWeaver;

    @FXML private VBox pendingDecisionMessagesContainer;
    @FXML private VBox pendingDecisionRequestsContainer;
    @FXML private Button refreshMessagesButton;

    public InProgressPaneController(DashboardShellViewModel viewModel, FxWeaver fxWeaver) {
        this.viewModel = viewModel;
        this.fxWeaver = fxWeaver;
    }

    @FXML
    void initialize() {
        refresh();
    }

    @FXML
    void onRefresh(){
        refresh();
    }

    /** Ricarica le card della tab Messages dalla sorgente dati. */
    @FXML
    public void refresh() {
        refreshPendingDecisionMessages();
        refreshPendingDecisionRequests();
    }

    private void refreshPendingDecisionRequests() {
        pendingDecisionRequestsContainer.getChildren().clear();

        List<PendingDecision> pendingDecisions = viewModel.getRequestPendingDecisions();

        if (pendingDecisions.isEmpty()) {
            Label emptyLabel = new Label("Nessuna decisione pendente.");
            emptyLabel.getStyleClass().add("panel-label");
            pendingDecisionRequestsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (PendingDecision pd : pendingDecisions) {
            FxControllerAndView<PendingDecisionCardController, Node> cardWrapper =
                    fxWeaver.load(PendingDecisionCardController.class);
            cardWrapper.getController().setData(pd, this::refresh);
            cardWrapper.getView().ifPresent(
                    view -> pendingDecisionRequestsContainer.getChildren().add(view));
        }
    }

    private void refreshPendingDecisionMessages() {
        pendingDecisionMessagesContainer.getChildren().clear();

        List<PendingDecision> pendingDecisions = viewModel.getMessagePendingDecisions();

        if (pendingDecisions.isEmpty()) {
            Label emptyLabel = new Label("Nessuna decisione pendente.");
            emptyLabel.getStyleClass().add("panel-label");
            pendingDecisionMessagesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (PendingDecision pd : pendingDecisions) {
            FxControllerAndView<PendingDecisionCardController, Node> cardWrapper =
                    fxWeaver.load(PendingDecisionCardController.class);
            cardWrapper.getController().setData(pd, this::refresh);
            cardWrapper.getView().ifPresent(
                    view -> pendingDecisionMessagesContainer.getChildren().add(view));
        }
    }
}

