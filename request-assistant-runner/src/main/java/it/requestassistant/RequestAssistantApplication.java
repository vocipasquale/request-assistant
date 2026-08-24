package it.requestassistant;

import it.requestassistant.batch.PlaygroundProcessOrchestrator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class RequestAssistantApplication extends Application {


    private ConfigurableApplicationContext context;
    private PlaygroundProcessOrchestrator playgroundProcessOrchestrator;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void init() {

        context = new SpringApplicationBuilder(
                RequestAssistantSpringConfiguration.class
        ).run();

        playgroundProcessOrchestrator = context.getBean(PlaygroundProcessOrchestrator.class);
    }

    @Override
    public void start(Stage stage) {
        ToggleButton batchSwitch = new ToggleButton("Avvia batch");
        Label statusLabel = new Label("Batch: STOPPED");

        batchSwitch.setOnAction(event -> {
            if (batchSwitch.isSelected()) {
                playgroundProcessOrchestrator.start();
                batchSwitch.setText("Stop batch");
                statusLabel.setText("Batch: RUNNING");
            } else {
                playgroundProcessOrchestrator.stop();
                batchSwitch.setText("Avvia batch");
                statusLabel.setText("Batch: STOPPED");
            }
        });

        VBox root = new VBox(12, batchSwitch, statusLabel);
        root.setPadding(new Insets(16));

        Scene scene = new Scene(root, 420, 180);

        stage.setTitle("Request Assistant");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> playgroundProcessOrchestrator.stop());

        stage.show();
    }

    @Override
    public void stop() {
        if (playgroundProcessOrchestrator != null) {
            playgroundProcessOrchestrator.stop();
        }
        context.close();
    }
}