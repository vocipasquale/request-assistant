package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.Action;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/action-pane.fxml")
public class ActionPaneController {

    private final DashboardShellViewModel viewModel;

   // @FXML private Label decisionOptionIdLabel;
    @FXML private Label confidenceLabel;
    @FXML private Label reasonsLabel;

    @FXML private Label actionTitleLabel;
  //  @FXML private Label actionStepsCountLabel;
    @FXML private ListView<String> actionStepsList;
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

      //  decisionOptionIdLabel.setText(String.valueOf(option.id()));
        confidenceLabel.setText(String.format("%.0f%%", option.getConfidence()));
        reasonsLabel.setText(option.getReasons() != null ? option.getReasons() : "-");

        Action action = option.getAction();
        actionTitleLabel.setText(action.getTitle().getTitle());
       // actionStepsCountLabel.setText(String.valueOf(action.steps().size()));
        actionStepsList.setItems(FXCollections.observableArrayList(action.getSteps()));
        viewMesssageButton.setDisable(message == null);
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

