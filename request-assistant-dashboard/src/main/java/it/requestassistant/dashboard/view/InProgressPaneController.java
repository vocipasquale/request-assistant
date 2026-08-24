package it.requestassistant.dashboard.view;

import javafx.fxml.FXML;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/it/requestassistant/dashboard/view/in-progress-pane.fxml")
public class InProgressPaneController {

    @FXML
    void initialize() {
        // Template iniziale: logica della sezione In progress da estendere.
    }
}

