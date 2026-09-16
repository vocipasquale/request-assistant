package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.Action;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/action-pane.fxml")
public class ActionPaneController {

    private final DashboardShellViewModel viewModel;

    @FXML private Label confidenceLabel;
    @FXML private Label reasonsLabel;

    @FXML private Label actionTitleLabel;
    @FXML private VBox actionContentContainer;
    @FXML private Button viewMesssageButton;


    private Runnable onProceed;
    private Stage stage;
    private Message message;

    public ActionPaneController(DashboardShellViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setData(DecisionOption option, Message message, Runnable onProceed, Stage stage) {
        this.onProceed = onProceed;
        this.stage = stage;
        this.message = message;

        confidenceLabel.setText(String.format("%.0f%%", option.getConfidence()));
        reasonsLabel.setText(option.getReasons() != null ? option.getReasons() : "-");

        Action action = option.getAction();
        if (action == null || action.getTitle() == null) {
            actionTitleLabel.setText("-");
            actionContentContainer.getChildren().clear();
            viewMesssageButton.setDisable(message == null);
            return;
        }

        actionTitleLabel.setText(action.getTitle().getTitle());
        actionContentContainer.getChildren().setAll(loadContentFor(action.getTitle()));
        viewMesssageButton.setDisable(message == null);

    }

    private Node loadContentFor(Action.Title title) {
        String resourcePath = switch (title) {
            case RISPONDI_A_MAIL, INOLTRA_MAIL -> "/it/requestassistant/dashboard/view/bozza-mail-pane.fxml";
            case NUOVA_RICHIESTA, MODIFICA_RICHIESTA, CHIUDI_RICHIESTA -> "/it/requestassistant/dashboard/view/bozza-richiesta-pane.fxml";
        };

        URL resource = Objects.requireNonNull(
                getClass().getResource(resourcePath),
                "Vista " + resourcePath + " non trovata");

        try {
            return new FXMLLoader(resource).load();
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare la vista " + resourcePath, e);
        }
    }


    @FXML
    void onCancel() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void onProceed() {
        if (onProceed != null) {
            onProceed.run();
        }
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void onViewMessage() {
        if (message != null) {
            viewModel.showMessage(message);
        }
    }
}

