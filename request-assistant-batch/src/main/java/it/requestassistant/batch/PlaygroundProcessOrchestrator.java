package it.requestassistant.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaygroundProcessOrchestrator {

    private static final long DEFAULT_POLL_INTERVAL_MS = 5000L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PlaygroundProcess playgroundProcess;
    private final TaskExecutor taskExecutor;
    private final long pollIntervalMs;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PlaygroundProcessOrchestrator(
            PlaygroundProcess playgroundProcess,
            TaskExecutor taskExecutor,
            @Value("${batch.playground.poll-interval-ms:" + DEFAULT_POLL_INTERVAL_MS + "}") long pollIntervalMs
    ) {
        this.playgroundProcess = playgroundProcess;
        this.taskExecutor = taskExecutor;
        this.pollIntervalMs = pollIntervalMs;
    }

    public boolean start() {
        if (!running.compareAndSet(false, true)) {
            logger.info("PlaygroundProcess già in esecuzione");
            return false;
        }

        logger.info("Avvio PlaygroundProcessOrchestrator");
        taskExecutor.execute(() -> {
            while (running.get()) {
                try {
                    playgroundProcess.runOnce();
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running.set(false);
                    logger.warn("PlaygroundProcess interrotto");
                } catch (Exception e) {
                    logger.error("Errore durante l'esecuzione del PlaygroundProcess", e);
                }
            }
            logger.info("PlaygroundProcessOrchestrator arrestato");
        });

        return true;
    }

    public boolean stop() {
        if (!running.compareAndSet(true, false)) {
            logger.info("PlaygroundProcess non in esecuzione");
            return false;
        }
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }
}

