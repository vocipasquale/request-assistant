package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/message-card.fxml")
public class MessageCardController {
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DashboardShellViewModel viewModel;

    @FXML
    private Label subjectLabel;
    @FXML private Label recivedAtLabel;
    @FXML private Button viewMesssageButton;


    public MessageCardController(DashboardShellViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    void initialize(){

    }

}
