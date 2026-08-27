package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.PendingDecision;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/pending-decision-card.fxml")
public class PendingDecisionCardController {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DashboardShellViewModel viewModel;

    @FXML private Label createdAtLabel;
    @FXML private Label targetLabel;
    @FXML private Button deleteCardButton;
    @FXML private TableView<DecisionOption> optionsTable;
    @FXML private TableColumn<DecisionOption, String> actionColumn;
    @FXML private TableColumn<DecisionOption, String> confidenceColumn;
    @FXML private TableColumn<DecisionOption, String> reasonColumn;
    @FXML private TableColumn<DecisionOption, Void> deleteOptionColumn;

    private PendingDecision pendingDecision;
    private Runnable onRefresh;

    public PendingDecisionCardController(DashboardShellViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    void initialize() {
        actionColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().action()));
        confidenceColumn.setCellValueFactory(
                data -> new SimpleStringProperty(String.format("%.0f%%", data.getValue().confidence())));
        reasonColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().reasons()));

        // Colonna con pulsante "Elimina" per ogni DecisionOption
        deleteOptionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Accetta");
            {
                btn.getStyleClass().add("accept-button");
                btn.setOnAction(e -> {
                    DecisionOption option = getTableView().getItems().get(getIndex());
                    viewModel.deleteDecisionOption(option.id());
                    if (onRefresh != null) onRefresh.run();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Placeholder quando la tabella è vuota
        optionsTable.setPlaceholder(new Label("Nessuna opzione disponibile."));
    }

    /**
     * Popola la card con i dati di una PendingDecision.
     *
     * @param decision  la PendingDecision da mostrare
     * @param onRefresh callback richiamato dopo accept/delete per aggiornare la lista
     */
    public void setData(PendingDecision decision, Runnable onRefresh) {
        this.pendingDecision = decision;
        this.onRefresh = onRefresh;

        createdAtLabel.setText(
                decision.getCreatedAt() != null ? decision.getCreatedAt().format(DATE_FMT) : "—");
        targetLabel.setText(
                decision.getTarget() != null ? decision.getTarget() : "—");
        optionsTable.setItems(FXCollections.observableArrayList(decision.getOptions()));
    }

    @FXML
    void onAccept() {
        viewModel.acceptPendingDecision(pendingDecision.getId());
        if (onRefresh != null) onRefresh.run();
    }

    @FXML
    void onDelete() {
        viewModel.deletePendingDecision(pendingDecision.getId());
        if (onRefresh != null) onRefresh.run();
    }
}

