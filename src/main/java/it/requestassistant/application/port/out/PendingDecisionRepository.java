package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.PendingDecision;

import java.util.List;
import java.util.Optional;

public interface PendingDecisionRepository {

    void save(PendingDecision decision);

    Optional<PendingDecision> findById(long id);

    List<PendingDecision> findByType(PendingDecision.Type type);

    void delete(long id);
}
