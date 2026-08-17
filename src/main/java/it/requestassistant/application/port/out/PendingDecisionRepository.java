//package it.requestassistant.application.port.out;
//
//import it.requestassistant.domain.model.PendingDecision;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
////@Repository
//public interface PendingDecisionRepository extends JpaRepository<PendingDecision, Long> {
//
//    //void save(PendingDecision decision);
//
//    Optional<PendingDecision> findById(long id);
//
//    List<PendingDecision> findByType(PendingDecision.Type type);
//
//    void delete(long id);
//}
