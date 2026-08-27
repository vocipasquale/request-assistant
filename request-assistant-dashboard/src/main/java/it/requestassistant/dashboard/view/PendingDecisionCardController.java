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
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/pending-decision-card.fxml")
public class PendingDecisionCardController {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DashboardShellViewModel viewModel;

    @FXML private Label createdAtLabel;
    @FXML private Label targetLabel;
    @FXML private Label subjectLabel;
    @FXML private Label senderLabel;
    @FXML private Label recivedAtLabel;
    @FXML private Label toLabel;
    @FXML private Label cCLabel;
    @FXML private Label topicLabel;
    @FXML private Label categoryLabel;
    @FXML private Label attachmentLabel;
    @FXML private Button deleteCardButton;
    @FXML private Button viewMesssageButton;
    @FXML private TableView<DecisionOption> optionsTable;
    @FXML private TableColumn<DecisionOption, String> actionColumn;
    @FXML private TableColumn<DecisionOption, String> confidenceColumn;
    @FXML private TableColumn<DecisionOption, String> reasonColumn;
    @FXML private TableColumn<DecisionOption, Void> acceptOptionColumn;

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

        // Colonna con pulsante "Accetta" per ogni DecisionOption
        acceptOptionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Accetta");
            {
                btn.getStyleClass().add("accept-button");
                btn.setOnAction(e -> {
                    DecisionOption option = getTableView().getItems().get(getIndex());
                   // viewModel.acceptOptionColumn(option.id());
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

        if (!Objects.isNull(decision.getMessage())) {
            subjectLabel.setText(decision.getMessage().subject() != null ? decision.getMessage().subject() : "—");
            senderLabel.setText(decision.getMessage().senderAddress() != null ? decision.getMessage().senderAddress() : "—");
            recivedAtLabel.setText(decision.getMessage().receivedAt() != null ? decision.getMessage().receivedAt().format(DATE_FMT) : "—");
            toLabel.setText(decision.getMessage().to() != null ? decision.getMessage().to() : "—");
            cCLabel.setText(decision.getMessage().cc() != null ? decision.getMessage().cc() : "—");
            topicLabel.setText(decision.getMessage().conversationTopic() != null ? decision.getMessage().conversationId() : "—");
            categoryLabel.setText(decision.getMessage().category() != null ? decision.getMessage().category() : "—");
            attachmentLabel.setText(decision.getMessage().hasAttachment() != null && decision.getMessage().hasAttachment() ? "Si" : "No");
        }
        optionsTable.setItems(FXCollections.observableArrayList(decision.getOptions()));
    }

    @FXML
    void onAccept() {
        viewModel.acceptDecisionOption(pendingDecision.getId());
        if (onRefresh != null) onRefresh.run();
    }

    @FXML
    void onDelete() {
        viewModel.deletePendingDecision(pendingDecision.getId());
        if (onRefresh != null) onRefresh.run();
    }

    @FXML
    void onViewMessage() {
        System.out.println("Visualizza messaggio associato alla decisione: " + pendingDecision.getMessage());
    }
}

