package it.requestassistant.batch;

import it.requestassistant.application.port.out.PlaygroundProcessControlPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaygroundProcessOrchestrator implements PlaygroundProcessControlPort {

    private static final long DEFAULT_POLL_INTERVAL_MS = 5000L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PlaygroundProcess playgroundProcess;
    private final TaskExecutor taskExecutor;
    private final long pollIntervalMs;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopRequsted = new AtomicBoolean(false);


    public PlaygroundProcessOrchestrator(
            PlaygroundProcess playgroundProcess,
            TaskExecutor taskExecutor,
            @Value("${batch.playground.poll-interval-ms:" + DEFAULT_POLL_INTERVAL_MS + "}") long pollIntervalMs
    ) {
        this.playgroundProcess = playgroundProcess;
        this.taskExecutor = taskExecutor;
        this.pollIntervalMs = pollIntervalMs;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            logger.info("PlaygroundProcess già in esecuzione");
            return;
        }

        logger.info("Avvio PlaygroundProcessOrchestrator");
        taskExecutor.execute(() -> {
            while (running.get() && !stopRequsted.get()) {
                try {
                    playgroundProcess.runOnce();
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException e) {
                    logger.warn("PlaygroundProcess interrotto");
                    running.set(false);
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    logger.error("Errore durante l'esecuzione del PlaygroundProcess", e);
                    running.set(false);
                    Thread.currentThread().interrupt();
                }
            }

            if (stopRequsted.compareAndSet(true, false)) {
                logger.info("PlaygroundProcessOrchestrator arrestato");
            }
        });
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            logger.info("PlaygroundProcess non in esecuzione");
            return;
        }

        if (stopRequsted.compareAndSet(false, true)) {
            logger.info("Richiesto STOP PlaygroundProcess!");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}

