package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardSection;
import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/it/requestassistant/dashboard/view/dashboard-shell.fxml")
public class DashboardShellController {

    private final DashboardShellViewModel viewModel;

    @FXML
    private Label currentSectionLabel;

    @FXML
    private VBox inProgressPane;

    @FXML
    private VBox batchPane;

    @FXML
    private VBox archivePane;

    public DashboardShellController(DashboardShellViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    void initialize() {
        viewModel.selectedSectionProperty().addListener((observable, oldValue, newValue) -> applySelectedSection(newValue));
        applySelectedSection(viewModel.selectedSectionProperty().get());
    }

    @FXML
    void onMenuInProgress() {
        viewModel.selectSection(DashboardSection.IN_PROGRESS);
    }

    @FXML
    void onMenuBatch() {
        viewModel.selectSection(DashboardSection.BATCH);
    }

    @FXML
    void onMenuArchive() {
        viewModel.selectSection(DashboardSection.ARCHIVE);
    }


    private void applySelectedSection(DashboardSection section) {
        boolean inProgressSelected = section == DashboardSection.IN_PROGRESS;
        boolean batchSelected = section == DashboardSection.BATCH;
        boolean archiveSelected = section == DashboardSection.ARCHIVE;

        inProgressPane.setManaged(inProgressSelected);
        inProgressPane.setVisible(inProgressSelected);
        batchPane.setManaged(batchSelected);
        batchPane.setVisible(batchSelected);
        archivePane.setManaged(archiveSelected);
        archivePane.setVisible(archiveSelected);
        currentSectionLabel.setText(switch (section) {
            case IN_PROGRESS -> "In progress";
            case BATCH -> "Batch";
            case ARCHIVE -> "Archive";
        });
    }
}

