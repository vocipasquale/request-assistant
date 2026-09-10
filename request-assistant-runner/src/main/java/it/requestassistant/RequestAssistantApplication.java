package it.requestassistant;

import it.requestassistant.dashboard.view.DashboardShellController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class RequestAssistantApplication extends Application {


    private ConfigurableApplicationContext context;
    private FxWeaver fxWeaver;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void init() {

        context = new SpringApplicationBuilder(
                RequestAssistantSpringConfiguration.class
        ).run();
        fxWeaver = context.getBean(FxWeaver.class);
    }

    @Override
    public void start(Stage stage) {
        Parent root = fxWeaver.loadView(DashboardShellController.class);
        Scene scene = new Scene(root, 960, 640);

        stage.setTitle("Request Assistant");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}