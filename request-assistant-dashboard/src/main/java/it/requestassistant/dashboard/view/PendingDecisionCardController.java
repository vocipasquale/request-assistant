package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.PendingDecision;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/pending-decision-card.fxml")
public class PendingDecisionCardController {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DashboardShellViewModel viewModel;
    private final FxWeaver fxWeaver;

    @FXML private Label createdAtLabel;
    @FXML private Label targetLabel;
    @FXML private Label subjectLabel;
    @FXML private Label recivedAtLabel;
    @FXML private HBox hBoxRequest;
    @FXML private Label rqCreatedAtLabel;
    @FXML private Label rqUpdatedAtLabel;
    @FXML private Label rqTitleLabel;
    @FXML private Label rqStatusLabel;
    @FXML private Button viewRequestButton;
    @FXML private Button viewMesssageButton;
    @FXML private Button deleteCardButton;
    @FXML private TableView<DecisionOption> optionsTable;
    @FXML private TableColumn<DecisionOption, String> actionColumn;
    @FXML private TableColumn<DecisionOption, String> confidenceColumn;
    @FXML private TableColumn<DecisionOption, String> reasonColumn;
    @FXML private TableColumn<DecisionOption, Void> acceptOptionColumn;

    private PendingDecision pendingDecision;
    private Runnable onRefresh;

    public PendingDecisionCardController(DashboardShellViewModel viewModel, FxWeaver fxWeaver) {
        this.viewModel = viewModel;
        this.fxWeaver = fxWeaver;
    }

    @FXML
    void initialize() {
        actionColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().action().title()));
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
                    openActionModal(option);
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
            recivedAtLabel.setText(decision.getMessage().receivedAt() != null ? decision.getMessage().receivedAt().format(DATE_FMT) : "—");
        }

        if (Objects.isNull(decision.getRequest())){
            hBoxRequest.setVisible(false);
        }else{
            hBoxRequest.setVisible(true);
            rqCreatedAtLabel.setText(decision.getRequest().getCreateAt() != null ? decision.getRequest().getCreateAt().format(DATE_FMT) : "—");
            rqUpdatedAtLabel.setText(decision.getRequest().getUpdateAt() != null ? decision.getRequest().getUpdateAt().format(DATE_FMT) : "—");
            rqTitleLabel.setText(decision.getRequest().getTitle() != null ? decision.getRequest().getTitle() : "—");
            rqStatusLabel.setText(decision.getRequest().getStatus() != null ? decision.getRequest().getStatus().toString() : "—");
        }

        optionsTable.setItems(FXCollections.observableArrayList(decision.getOptions()));
    }

//    @FXML
//    void onAccept() {
//        viewModel.acceptDecisionOption(pendingDecision.getId());
//        if (onRefresh != null) onRefresh.run();
//    }

    @FXML
    void onDelete() {
        viewModel.deletePendingDecision(pendingDecision);
        if (onRefresh != null) onRefresh.run();
    }

    @FXML
    void onViewMessage() {
        viewModel.showMessage(pendingDecision.getMessage());
    }

    @FXML
    void onViewRequest() {
        System.out.println("Mostra REQUEST!");
    }

    private void openActionModal(DecisionOption option) {
        FxControllerAndView<ActionPaneController, Node> wrapper = fxWeaver.load(ActionPaneController.class);
        ActionPaneController controller = wrapper.getController();
        Parent modalRoot = (Parent) wrapper.getView().orElseThrow(
                () -> new IllegalStateException("Vista action-pane.fxml non disponibile"));

        Stage modalStage = new Stage();
        modalStage.setTitle("Conferma azione");
        modalStage.initModality(Modality.WINDOW_MODAL);

        Window owner = optionsTable.getScene() != null ? optionsTable.getScene().getWindow() : null;
        if (owner != null) {
            modalStage.initOwner(owner);
        }

        Scene scene = new Scene(modalRoot);
        URL stylesheetUrl = Objects.requireNonNull(
                getClass().getResource("/it/requestassistant/dashboard/styles/dashboard.css"),
                "Foglio di stile dashboard.css non trovato");
        String stylesheet = stylesheetUrl.toExternalForm();
        scene.getStylesheets().add(stylesheet);
        modalStage.setScene(scene);

        controller.setData(option, pendingDecision.getMessage(), () -> {
            viewModel.acceptDecisionOption(pendingDecision, option);
            if (onRefresh != null) {
                onRefresh.run();
            }
        }, modalStage);

        modalStage.showAndWait();
    }
}

