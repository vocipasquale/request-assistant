package it.requestassistant.application.port.out;

public interface PlaygroundProcessControlPort {

    void start();

    void stop();

    boolean isRunning();
}
