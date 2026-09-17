package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.util.DataActionJsonConverter;
import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.Action;
import it.requestassistant.domain.model.DataAction;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
        actionContentContainer.getChildren().setAll(loadContentFor(action));
        viewMesssageButton.setDisable(message == null);

    }

    private Node loadContentFor(Action action) {
        Action.Title title = action.getTitle();
        String resourcePath = switch (title) {
            case RISPONDI_A_MAIL, INOLTRA_MAIL -> "/it/requestassistant/dashboard/view/bozza-mail-pane.fxml";
            case NUOVA_RICHIESTA, MODIFICA_RICHIESTA, CHIUDI_RICHIESTA -> "/it/requestassistant/dashboard/view/bozza-richiesta-pane.fxml";
        };

        URL resource = Objects.requireNonNull(
                getClass().getResource(resourcePath),
                "Vista " + resourcePath + " non trovata");

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Node content = loader.load();
            if (title == Action.Title.RISPONDI_A_MAIL || title == Action.Title.INOLTRA_MAIL) {
                populateMailDraftFields(loader, action.getAiResponse());
            } else {
                populateRequestDraftFields(loader, action.getAiResponse());
            }
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare la vista " + resourcePath, e);
        }
    }

    private void populateMailDraftFields(FXMLLoader loader, String aiResponse) {
        DataAction dataAction = DataActionJsonConverter.fromJson(aiResponse);
        Message draftMessage = dataAction != null ? dataAction.message() : null;

        TextField mittenteField = (TextField) loader.getNamespace().get("mittenteField");
        TextField destinatarioField = (TextField) loader.getNamespace().get("destinatarioField");
        TextField oggettoField = (TextField) loader.getNamespace().get("oggettoField");
        TextArea testoArea = (TextArea) loader.getNamespace().get("testoArea");

        if (mittenteField != null) {
            mittenteField.setText(draftMessage != null && draftMessage.getSenderAddress() != null
                    ? draftMessage.getSenderAddress()
                    : "");
        }
        if (destinatarioField != null) {
            destinatarioField.setText(draftMessage != null && draftMessage.getTo() != null
                    ? draftMessage.getTo()
                    : "");
        }
        if (oggettoField != null) {
            oggettoField.setText(draftMessage != null && draftMessage.getSubject() != null
                    ? draftMessage.getSubject()
                    : "");
        }
        if (testoArea != null) {
            testoArea.setText(draftMessage != null && draftMessage.getBodyText() != null
                    ? draftMessage.getBodyText()
                    : "");
        }
    }

    private void populateRequestDraftFields(FXMLLoader loader, String aiResponse) {
        DataAction dataAction = DataActionJsonConverter.fromJson(aiResponse);
        Request draftRequest = dataAction != null ? dataAction.request() : null;
        User draftUser = draftRequest != null ? draftRequest.getUser() : null;

        TextField titoloField = (TextField) loader.getNamespace().get("titoloField");
        TextField statoField = (TextField) loader.getNamespace().get("statoField");
        TextArea noteArea = (TextArea) loader.getNamespace().get("noteArea");
        TextField cognomeField = (TextField) loader.getNamespace().get("cognomeField");
        TextField nomeField = (TextField) loader.getNamespace().get("nomeField");
        TextField codiceFiscaleField = (TextField) loader.getNamespace().get("codiceFiscaleField");
        TextField emailField = (TextField) loader.getNamespace().get("emailField");
        TextField utenzaField = (TextField) loader.getNamespace().get("utenzaField");
        VBox requestItemsContainer = (VBox) loader.getNamespace().get("requestItemsContainer");
        VBox messagesContainer = (VBox) loader.getNamespace().get("messagesContainer");

        if (titoloField != null) {
            titoloField.setText(draftRequest != null && draftRequest.getTitle() != null
                    ? draftRequest.getTitle()
                    : "");
        }
        if (statoField != null) {
            statoField.setText(draftRequest != null && draftRequest.getStatus() != null
                    ? draftRequest.getStatus().name()
                    : "");
        }
        if (noteArea != null) {
            noteArea.setText(draftRequest != null && draftRequest.getNote() != null
                    ? draftRequest.getNote()
                    : "");
        }
        if (cognomeField != null) {
            cognomeField.setText(draftUser != null && draftUser.getCognome() != null
                    ? draftUser.getCognome()
                    : "");
        }
        if (nomeField != null) {
            nomeField.setText(draftUser != null && draftUser.getNome() != null
                    ? draftUser.getNome()
                    : "");
        }
        if (codiceFiscaleField != null) {
            codiceFiscaleField.setText(draftUser != null && draftUser.getCodiceFiscale() != null
                    ? draftUser.getCodiceFiscale()
                    : "");
        }
        if (emailField != null) {
            emailField.setText(draftUser != null && draftUser.getEmail() != null
                    ? draftUser.getEmail()
                    : "");
        }
        if (utenzaField != null) {
            utenzaField.setText(draftUser != null && draftUser.getUtenza() != null
                    ? draftUser.getUtenza()
                    : "");
        }

        populateRequestItems(requestItemsContainer, draftRequest != null ? draftRequest.getItems() : null);
        populateMessages(messagesContainer, draftRequest != null ? draftRequest.getMessages() : null);
    }

    private void populateRequestItems(VBox container, List<RequestItem> items) {
        if (container == null) {
            return;
        }

        container.getChildren().clear();
        if (items == null || items.isEmpty()) {
            container.getChildren().add(createPlaceholderLabel("Nessun request item disponibile"));
            return;
        }

        int index = 1;
        for (RequestItem item : items) {
            if (item == null) {
                continue;
            }

            VBox itemBox = createItemBox("Request item " + index++);
            itemBox.getChildren().add(createLabeledTextField("Tipo:", item.getType() != null ? item.getType().name() : ""));
            itemBox.getChildren().add(createLabeledTextField("Stato:", item.getStatus() != null ? item.getStatus().name() : ""));
            itemBox.getChildren().add(createLabeledTextField("Ticket:", safeText(item.getTicket())));
            itemBox.getChildren().add(createLabeledTextField("Ambiente:", formatAmbienti(item.getAmbiente())));
            itemBox.getChildren().add(createLabeledTextArea("Dettaglio:", safeText(item.getDettaglio()), 3));
            itemBox.getChildren().add(createLabeledTextArea("Nota:", safeText(item.getNota()), 3));
            container.getChildren().add(itemBox);
        }

        if (container.getChildren().isEmpty()) {
            container.getChildren().add(createPlaceholderLabel("Nessun request item disponibile"));
        }
    }

    private void populateMessages(VBox container, List<Message> messages) {
        if (container == null) {
            return;
        }

        container.getChildren().clear();
        if (messages == null || messages.isEmpty()) {
            container.getChildren().add(createPlaceholderLabel("Nessun messaggio disponibile"));
            return;
        }

        int index = 1;
        for (Message item : messages) {
            if (item == null) {
                continue;
            }

            VBox messageBox = createItemBox("Messaggio " + index++);
            messageBox.getChildren().add(createLabeledTextField("Oggetto:", safeText(item.getSubject())));
            messageBox.getChildren().add(createLabeledTextField("Da:", safeText(item.getSenderAddress())));
            messageBox.getChildren().add(createLabeledTextField("A:", safeText(item.getTo())));
            messageBox.getChildren().add(createLabeledTextField("Cc:", safeText(item.getCc())));
            messageBox.getChildren().add(createLabeledTextArea("Testo:", safeText(item.getBodyText()), 4));
            container.getChildren().add(messageBox);
        }

        if (container.getChildren().isEmpty()) {
            container.getChildren().add(createPlaceholderLabel("Nessun messaggio disponibile"));
        }
    }

    private VBox createItemBox(String title) {
        VBox box = new VBox(8.0);
        box.setPadding(new Insets(10.0));
        box.setStyle("-fx-border-color: #D9D9D9; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-field-label");
        box.getChildren().add(titleLabel);
        return box;
    }

    private Label createPlaceholderLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        return label;
    }

    private HBox createLabeledTextField(String labelText, String value) {
        Label label = new Label(labelText);
        label.getStyleClass().add("card-field-label");
        label.setMinWidth(110.0);

        TextField field = new TextField(value);
        field.getStyleClass().add("mail-editable-field");
        field.setStyle("-fx-control-inner-background: white; -fx-background-color: white;");
        HBox.setHgrow(field, Priority.ALWAYS);

        HBox row = new HBox(8.0, label, field);
        return row;
    }

    private VBox createLabeledTextArea(String labelText, String value, int prefRowCount) {
        Label label = new Label(labelText);
        label.getStyleClass().add("card-field-label");

        TextArea area = new TextArea(value);
        area.getStyleClass().add("mail-editable-field");
        area.setStyle("-fx-control-inner-background: white; -fx-background-color: white;");
        area.setWrapText(true);
        area.setPrefRowCount(prefRowCount);

        VBox box = new VBox(6.0, label, area);
        return box;
    }

    private String formatAmbienti(List<RequestItem.Ambiente> ambienti) {
        if (ambienti == null || ambienti.isEmpty()) {
            return "";
        }
        return ambienti.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String safeText(String value) {
        return value != null ? value : "";
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

