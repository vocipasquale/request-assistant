package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/it/requestassistant/dashboard/view/batch-pane.fxml")
public class BatchPaneController {

    private final DashboardShellViewModel viewModel;

    @FXML
    private ToggleButton batchToggleButton;

    @FXML
    private Label batchStatusLabel;

    public BatchPaneController(DashboardShellViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    void initialize() {
        batchStatusLabel.textProperty().bind(viewModel.batchStatusTextProperty());
        batchToggleButton.textProperty().bind(viewModel.batchToggleTextProperty());
        viewModel.batchRunningProperty().addListener((observable, oldValue, newValue) -> batchToggleButton.setSelected(newValue));
        batchToggleButton.setSelected(viewModel.batchRunningProperty().get());
    }

    @FXML
    void onToggleBatch() {
        viewModel.toggleBatch();
    }
}

