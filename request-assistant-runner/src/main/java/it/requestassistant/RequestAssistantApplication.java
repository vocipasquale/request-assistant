package it.requestassistant;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class RequestAssistantApplication extends Application {


    private ConfigurableApplicationContext context;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void init() {

        context = new SpringApplicationBuilder(
                RequestAssistantSpringConfiguration.class
        ).run();
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Request Assistant");
        stage.setWidth(800);
        stage.setHeight(600);

        stage.show();
    }

    @Override
    public void stop() {
        context.close();
    }
}