package it.requestassistant.dashboard.view;

import it.requestassistant.dashboard.util.DataActionJsonConverter;
import it.requestassistant.dashboard.util.ActionPaneDraftHelper;
import it.requestassistant.dashboard.viewmodel.DashboardShellViewModel;
import it.requestassistant.domain.model.*;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("/it/requestassistant/dashboard/view/action-pane.fxml")
public class ActionPaneController {

    private static final Logger logger = LoggerFactory.getLogger(ActionPaneController.class);

    private final DashboardShellViewModel viewModel;

    @FXML private Label confidenceLabel;
    @FXML private Label reasonsLabel;
    @FXML private Label actionTitleLabel;
    @FXML private VBox actionContentContainer;
    @FXML private Button viewMesssageButton;
    @FXML private Button annullaButton;
    @FXML private Button fattoButton;

    private Runnable onProceed;
    private Stage stage;
    private PendingDecision pendingDecision;
    private DataAction dataAction;
    private Action.Title currentActionTitle;

    private TextField mittenteField;
    private TextField destinatarioField;
    private TextField oggettoField;
    private TextArea testoArea;

    private TextField titoloField;
    private TextField statoField;
    private TextArea noteArea;
    private TextField cognomeField;
    private TextField nomeField;
    private TextField codiceFiscaleField;
    private TextField emailField;
    private TextField utenzaField;

    private final List<RequestItemRowControls> requestItemRows = new ArrayList<>();
    private final List<MessageRowControls> messageRows = new ArrayList<>();

    public ActionPaneController(DashboardShellViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setData(DecisionOption option, PendingDecision pendingDecision, DataAction dataAction, Runnable onProceed, Stage stage) {
        this.onProceed = onProceed;
        this.stage = stage;
        this.pendingDecision = pendingDecision;
        this.dataAction = dataAction;
        resetDraftControlReferences();

        logger.debug("Inizializzazione modale azione per pending decision {} e opzione {}",
                pendingDecision != null ? pendingDecision.getId() : null,
                option != null ? option.getId() : null);

        confidenceLabel.setText(String.format("%.0f%%", option.getConfidence()));
        reasonsLabel.setText(option.getReasons() != null ? option.getReasons() : "-");

        Action action = option.getAction();
        currentActionTitle = action != null ? action.getTitle() : null;
        if (action == null || action.getTitle() == null) {
            logger.warn("Azione non disponibile per la pending decision {}",
                    pendingDecision != null ? pendingDecision.getId() : null);
            actionTitleLabel.setText("-");
            actionContentContainer.getChildren().clear();
            viewMesssageButton.setDisable(pendingDecision.getMessage() == null);
            return;
        }

        logger.debug("Caricamento pannello per azione {}", action.getTitle().name());
        actionTitleLabel.setText(action.getTitle().getTitle());
        actionContentContainer.getChildren().setAll(loadContentFor(action));
        viewMesssageButton.setDisable(pendingDecision.getMessage() == null);
    }

    public DataAction getDataAction() {
        return dataAction;
    }

    private Node loadContentFor(Action action) {
        Action.Title title = action.getTitle();
        String resourcePath = switch (title) {
            case RISPONDI_A_MAIL -> "/it/requestassistant/dashboard/view/bozza-mail-pane.fxml";
            case NUOVA_RICHIESTA, MODIFICA_RICHIESTA -> "/it/requestassistant/dashboard/view/bozza-richiesta-pane.fxml";
            default -> throw new IllegalStateException("Unexpected value: " + title);
        };

        URL resource = Objects.requireNonNull(
                getClass().getResource(resourcePath),
                "Vista " + resourcePath + " non trovata");

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Node content = loader.load();
            if (title == Action.Title.RISPONDI_A_MAIL) {
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
        DataAction resolvedDataAction = resolveDataAction(aiResponse);
        Message draftMessage = resolvedDataAction != null ? resolvedDataAction.message() : null;

        logger.debug("Popolamento pannello bozza mail");

        mittenteField = (TextField) loader.getNamespace().get("mittenteField");
        destinatarioField = (TextField) loader.getNamespace().get("destinatarioField");
        oggettoField = (TextField) loader.getNamespace().get("oggettoField");
        testoArea = (TextArea) loader.getNamespace().get("testoArea");

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
        DataAction resolvedDataAction = resolveDataAction(aiResponse);
        Request draftRequest = resolvedDataAction != null ? resolvedDataAction.request() : null;
        User draftUser = draftRequest != null ? draftRequest.getUser() : null;

        logger.debug("Popolamento pannello bozza richiesta");

        titoloField = (TextField) loader.getNamespace().get("titoloField");
        statoField = (TextField) loader.getNamespace().get("statoField");
        noteArea = (TextArea) loader.getNamespace().get("noteArea");
        cognomeField = (TextField) loader.getNamespace().get("cognomeField");
        nomeField = (TextField) loader.getNamespace().get("nomeField");
        codiceFiscaleField = (TextField) loader.getNamespace().get("codiceFiscaleField");
        emailField = (TextField) loader.getNamespace().get("emailField");
        utenzaField = (TextField) loader.getNamespace().get("utenzaField");
        GridPane requestItemsGrid = (GridPane) loader.getNamespace().get("requestItemsGrid");
        GridPane messagesGrid = (GridPane) loader.getNamespace().get("messagesGrid");

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

        populateRequestItemsTable(requestItemsGrid, draftRequest != null ? draftRequest.getItems() : null);
        populateMessagesTable(messagesGrid, draftRequest != null ? draftRequest.getMessages() : null);
    }

    private void populateRequestItemsTable(GridPane grid, List<RequestItem> items) {
        requestItemRows.clear();

        if (grid == null) {
            return;
        }

        resetGrid(grid);
        configureRequestItemsColumns(grid);

        if (items == null || items.isEmpty()) {
            addEmptyTableMessage(grid, "Nessun request item disponibile", 7);
            return;
        }

        addRequestItemsHeaderRow(grid);

        int rowIndex = 1;
        int index = 1;
        for (RequestItem item : items) {
            if (item == null) {
                continue;
            }

            addGridCell(grid, createTableRowLabel("Request item " + index++), 0, rowIndex);
            TextField typeField = createTableTextField(item.getType() != null ? item.getType().name() : "");
            TextField statusField = createTableTextField(item.getStatus() != null ? item.getStatus().name() : "");
            TextField ticketField = createTableTextField(ActionPaneDraftHelper.safeText(item.getTicket()));
            TextField ambienteField = createTableTextField(ActionPaneDraftHelper.formatAmbienti(item.getAmbiente()));
            TextArea dettaglioArea = createTableTextArea(ActionPaneDraftHelper.safeText(item.getDettaglio()), 2);
            TextArea notaArea = createTableTextArea(ActionPaneDraftHelper.safeText(item.getNota()), 2);

            addGridCell(grid, typeField, 1, rowIndex);
            addGridCell(grid, statusField, 2, rowIndex);
            addGridCell(grid, ticketField, 3, rowIndex);
            addGridCell(grid, ambienteField, 4, rowIndex);
            addGridCell(grid, dettaglioArea, 5, rowIndex);
            addGridCell(grid, notaArea, 6, rowIndex);

            requestItemRows.add(new RequestItemRowControls(
                    item,
                    typeField,
                    statusField,
                    ticketField,
                    ambienteField,
                    dettaglioArea,
                    notaArea
            ));
            rowIndex++;
        }

        if (rowIndex == 1) {
            resetGrid(grid);
            configureRequestItemsColumns(grid);
            addEmptyTableMessage(grid, "Nessun request item disponibile", 7);
        }
    }

    private void populateMessagesTable(GridPane grid, List<Message> messages) {
        messageRows.clear();

        if (grid == null) {
            return;
        }

        resetGrid(grid);
        configureMessagesColumns(grid);

        if (messages == null || messages.isEmpty()) {
            addEmptyTableMessage(grid, "Nessun messaggio disponibile", 6);
            return;
        }

        addMessagesHeaderRow(grid);

        int rowIndex = 1;
        int index = 1;
        for (Message item : messages) {
            if (item == null) {
                continue;
            }

            addGridCell(grid, createTableRowLabel("Messaggio " + index++), 0, rowIndex);
            TextField subjectField = createTableTextField(ActionPaneDraftHelper.safeText(item.getSubject()));
            TextField senderField = createTableTextField(ActionPaneDraftHelper.safeText(item.getSenderAddress()));
            TextField toField = createTableTextField(ActionPaneDraftHelper.safeText(item.getTo()));
            TextField ccField = createTableTextField(ActionPaneDraftHelper.safeText(item.getCc()));
            TextArea bodyArea = createTableTextArea(ActionPaneDraftHelper.safeText(item.getBodyText()), 3);

            addGridCell(grid, subjectField, 1, rowIndex);
            addGridCell(grid, senderField, 2, rowIndex);
            addGridCell(grid, toField, 3, rowIndex);
            addGridCell(grid, ccField, 4, rowIndex);
            addGridCell(grid, bodyArea, 5, rowIndex);

            messageRows.add(new MessageRowControls(
                    item,
                    subjectField,
                    senderField,
                    toField,
                    ccField,
                    bodyArea
            ));
            rowIndex++;
        }

        if (rowIndex == 1) {
            resetGrid(grid);
            configureMessagesColumns(grid);
            addEmptyTableMessage(grid, "Nessun messaggio disponibile", 6);
        }
    }

    private void addRequestItemsHeaderRow(GridPane grid) {
        addGridCell(grid, createHeaderLabel("Item"), 0, 0);
        addGridCell(grid, createHeaderLabel("Tipo"), 1, 0);
        addGridCell(grid, createHeaderLabel("Stato"), 2, 0);
        addGridCell(grid, createHeaderLabel("Ticket"), 3, 0);
        addGridCell(grid, createHeaderLabel("Ambiente"), 4, 0);
        addGridCell(grid, createHeaderLabel("Dettaglio"), 5, 0);
        addGridCell(grid, createHeaderLabel("Nota"), 6, 0);
    }

    private void addMessagesHeaderRow(GridPane grid) {
        addGridCell(grid, createHeaderLabel("Messaggio"), 0, 0);
        addGridCell(grid, createHeaderLabel("Oggetto"), 1, 0);
        addGridCell(grid, createHeaderLabel("Da"), 2, 0);
        addGridCell(grid, createHeaderLabel("A"), 3, 0);
        addGridCell(grid, createHeaderLabel("Cc"), 4, 0);
        addGridCell(grid, createHeaderLabel("Testo"), 5, 0);
    }

    private void configureRequestItemsColumns(GridPane grid) {
        grid.getColumnConstraints().addAll(
                createColumnConstraint(130.0),
                createColumnConstraint(130.0),
                createColumnConstraint(130.0),
                createColumnConstraint(130.0),
                createColumnConstraint(160.0),
                createGrowingColumnConstraint(260.0),
                createGrowingColumnConstraint(260.0)
        );
    }

    private void configureMessagesColumns(GridPane grid) {
        grid.getColumnConstraints().addAll(
                createColumnConstraint(130.0),
                createGrowingColumnConstraint(220.0),
                createColumnConstraint(170.0),
                createColumnConstraint(170.0),
                createColumnConstraint(170.0),
                createGrowingColumnConstraint(320.0)
        );
    }

    private ColumnConstraints createColumnConstraint(double prefWidth) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPrefWidth(prefWidth);
        constraints.setMinWidth(prefWidth);
        return constraints;
    }

    private ColumnConstraints createGrowingColumnConstraint(double prefWidth) {
        ColumnConstraints constraints = createColumnConstraint(prefWidth);
        constraints.setHgrow(Priority.ALWAYS);
        constraints.setFillWidth(true);
        return constraints;
    }

    private void resetGrid(GridPane grid) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
    }

    private void addEmptyTableMessage(GridPane grid, String text, int columnCount) {
        Label label = new Label(text);
        label.setWrapText(true);
        grid.add(label, 0, 0);
        GridPane.setColumnSpan(label, columnCount);
    }

    private Label createHeaderLabel(String labelText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("card-field-label");
        return label;
    }

    private Label createTableRowLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-field-label");
        label.setWrapText(true);
        return label;
    }

    private TextField createTableTextField(String value) {
        TextField field = new TextField(value);
        field.getStyleClass().add("mail-editable-field");
        field.setStyle("-fx-control-inner-background: white; -fx-background-color: white;");
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private TextArea createTableTextArea(String value, int prefRowCount) {
        TextArea area = new TextArea(value);
        area.getStyleClass().add("mail-editable-field");
        area.setStyle("-fx-control-inner-background: white; -fx-background-color: white;");
        area.setWrapText(true);
        area.setPrefRowCount(prefRowCount);
        area.setMaxWidth(Double.MAX_VALUE);
        return area;
    }

    private void addGridCell(GridPane grid, Node node, int columnIndex, int rowIndex) {
        grid.add(node, columnIndex, rowIndex);
        GridPane.setHgrow(node, Priority.ALWAYS);
        GridPane.setFillWidth(node, true);
    }

    private DataAction resolveDataAction(String aiResponse) {
        if (dataAction == null) {
            logger.debug("Conversione di aiResponse in DataAction");
            dataAction = DataActionJsonConverter.fromJson(aiResponse);
        }
        return dataAction;
    }

    private void resetDraftControlReferences() {
        mittenteField = null;
        destinatarioField = null;
        oggettoField = null;
        testoArea = null;
        titoloField = null;
        statoField = null;
        noteArea = null;
        cognomeField = null;
        nomeField = null;
        codiceFiscaleField = null;
        emailField = null;
        utenzaField = null;
        requestItemRows.clear();
        messageRows.clear();
    }


    @FXML
    void onCancel() {
        logger.debug("Chiusura modale azione senza conferma");
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void onProceed() throws Exception {
        logger.info("Conferma dell'azione dalla modale");
        if (onProceed != null) {
            aggiornaPendingPecision();
            onProceed.run();
        }
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * prima di inviare al BE la pending decision da
     */
    private void aggiornaPendingPecision() throws Exception {
        if (currentActionTitle == null) {
            logger.warn("Impossibile aggiornare il draft: titolo azione non disponibile");
            return;
        }

        logger.debug("Aggiornamento DataAction dalla dashboard per azione {}", currentActionTitle.name());

        switch (currentActionTitle) {
            case RISPONDI_A_MAIL -> aggiornaDataActionMail();
            case NUOVA_RICHIESTA, MODIFICA_RICHIESTA -> aggiornaDataActionRichiesta();
            default -> {
                throw new Exception("Tipo di azione non gestito: " + currentActionTitle.getTitle());
            }
        }
    }

    private void aggiornaDataActionMail() {
        logger.debug("Aggiornamento DataAction per azione RISPONDI_A_MAIL");
        Message updatedMessage = dataAction != null && dataAction.message() != null
                ? dataAction.message()
                : ActionPaneDraftHelper.copyMessage(dataAction != null ? dataAction.message() : null);
        updatedMessage.setSenderAddress(textOf(mittenteField));
        updatedMessage.setTo(textOf(destinatarioField));
        updatedMessage.setSubject(textOf(oggettoField));
        updatedMessage.setBodyText(textOf(testoArea));

        dataAction = new DataAction(updatedMessage, null);
        logger.debug("Bozza mail aggiornata con i dati inseriti dall'utente");
    }

    private void aggiornaDataActionRichiesta() {
        logger.debug("Aggiornamento DataAction per azione NUOVA_RICHIESTA/MODIFICA_RICHIESTA");
        Request updatedRequest = dataAction != null && dataAction.request() != null
                ? dataAction.request()
                : ActionPaneDraftHelper.copyRequest(dataAction != null ? dataAction.request() : null);
        updatedRequest.setTitle(textOf(titoloField));
        updatedRequest.setStatus(ActionPaneDraftHelper.parseEnum(Request.Status.class, textOf(statoField), "stato richiesta"));
        updatedRequest.setNote(textOf(noteArea));

        User updatedUser = updatedRequest.getUser() != null
                ? updatedRequest.getUser()
                : ActionPaneDraftHelper.copyUser(null);
        updatedUser.setCognome(textOf(cognomeField));
        updatedUser.setNome(textOf(nomeField));
        updatedUser.setCodiceFiscale(textOf(codiceFiscaleField));
        updatedUser.setEmail(textOf(emailField));
        updatedUser.setUtenza(textOf(utenzaField));
        updatedRequest.setUser(updatedUser);

        updatedRequest.setItems(buildUpdatedRequestItems());
        updatedRequest.setMessages(buildUpdatedMessages());

        dataAction = new DataAction(null, updatedRequest);
        logger.debug("Bozza richiesta aggiornata con i dati inseriti dall'utente");
    }

    private List<RequestItem> buildUpdatedRequestItems() {
        List<RequestItem> items = new ArrayList<>();

        for (RequestItemRowControls row : requestItemRows) {
            RequestItem updatedItem = ActionPaneDraftHelper.copyRequestItem(row.source);
            updatedItem.setType(ActionPaneDraftHelper.parseEnum(RequestItem.Type.class, textOf(row.typeField), "tipo request item"));
            updatedItem.setStatus(ActionPaneDraftHelper.parseEnum(RequestItem.Status.class, textOf(row.statusField), "stato request item"));
            updatedItem.setTicket(textOf(row.ticketField));
            updatedItem.setAmbiente(ActionPaneDraftHelper.parseAmbienti(textOf(row.ambienteField)));
            updatedItem.setDettaglio(textOf(row.dettaglioArea));
            updatedItem.setNota(textOf(row.notaArea));
            items.add(updatedItem);
        }

        return items;
    }

    private List<Message> buildUpdatedMessages() {
        List<Message> messages = new ArrayList<>();

        for (MessageRowControls row : messageRows) {
            Message updatedMessage = ActionPaneDraftHelper.copyMessage(row.source);
            updatedMessage.setSubject(textOf(row.subjectField));
            updatedMessage.setSenderAddress(textOf(row.senderField));
            updatedMessage.setTo(textOf(row.toField));
            updatedMessage.setCc(textOf(row.ccField));
            updatedMessage.setBodyText(textOf(row.bodyArea));
            messages.add(updatedMessage);
        }

        return messages;
    }

    private String textOf(TextField field) {
        return field != null ? ActionPaneDraftHelper.safeText(field.getText()) : "";
    }

    private String textOf(TextArea area) {
        return area != null ? ActionPaneDraftHelper.safeText(area.getText()) : "";
    }

    private static final class RequestItemRowControls {
        private final RequestItem source;
        private final TextField typeField;
        private final TextField statusField;
        private final TextField ticketField;
        private final TextField ambienteField;
        private final TextArea dettaglioArea;
        private final TextArea notaArea;

        private RequestItemRowControls(RequestItem source, TextField typeField, TextField statusField,
                                       TextField ticketField, TextField ambienteField,
                                       TextArea dettaglioArea, TextArea notaArea) {
            this.source = source;
            this.typeField = typeField;
            this.statusField = statusField;
            this.ticketField = ticketField;
            this.ambienteField = ambienteField;
            this.dettaglioArea = dettaglioArea;
            this.notaArea = notaArea;
        }
    }

    private static final class MessageRowControls {
        private final Message source;
        private final TextField subjectField;
        private final TextField senderField;
        private final TextField toField;
        private final TextField ccField;
        private final TextArea bodyArea;

        private MessageRowControls(Message source, TextField subjectField, TextField senderField,
                                   TextField toField, TextField ccField, TextArea bodyArea) {
            this.source = source;
            this.subjectField = subjectField;
            this.senderField = senderField;
            this.toField = toField;
            this.ccField = ccField;
            this.bodyArea = bodyArea;
        }
    }


    @FXML
    void onViewMessage() {
        if (pendingDecision.getMessage() != null) {
            logger.debug("Apertura messaggio associato alla pending decision {}", pendingDecision.getId());
            viewModel.showMessage(pendingDecision.getMessage());
        }
    }
}

