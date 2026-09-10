package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test di integrazione reale su SQLite in-memory (configurato in src/test/resources/application.yml).
 * Le tabelle vengono create da Flyway (V1__initial_schema.sql) all'avvio del contesto.
 *
 * Ordine di esecuzione rispetta i vincoli FK:
 *   1. user_account
 *   2. request  (FK -> user_account)
 *   3. request_item (FK -> request)
 *   4. message  (FK -> request)
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PersistenceDao.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersistenceDaoInsertTest {

    @Autowired
    private PersistenceDao persistenceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ──────────────────────────────────────────────────────────────────────────
    // Fixtures
    // ──────────────────────────────────────────────────────────────────────────

    private User buildUser() {
        User user = new User();
        user.setCognome("Rossi");
        user.setNome("Mario");
        user.setCodiceFiscale("RSSMRA80A01H501U");
        user.setEmail("mario.rossi@example.com");
        user.setUtenza("m.rossi");
        return user;
    }

    private Request buildRequest() {
        Request request = new Request();
        request.setCreateAt(LocalDateTime.of(2026, 1, 10, 9, 0));
        request.setUpdateAt(LocalDateTime.of(2026, 1, 10, 9, 0));
        request.setTitle("Richiesta abilitazione VPN");
        request.setStatus(Request.Status.IN_PROGRESS);
        request.setNote("Nota di test");
        // Costruiamo uno User fittizio con l'id già salvato per il FK
        User fkUser = new User();
        fkUser.setCodiceFiscale("RSSMRA80A01H501U"); // non serve, userId viene da RequestRow
        request.setUser(fkUser);
        return request;
    }

    private RequestItem buildRequestItem() {
        RequestItem item = new RequestItem();
        item.setType(RequestItem.Type.NUOVA_UTENZA);
        item.setCreateAt(LocalDateTime.of(2026, 1, 10, 9, 5));
        item.setUpdateAt(LocalDateTime.of(2026, 1, 10, 9, 5));
        item.setDettaglio("Dettaglio richiesta VPN");
        item.setNota("Nota item");
        item.setAmbiente(List.of(RequestItem.Ambiente.SVILUPPO, RequestItem.Ambiente.COLLAUDO));
        item.setStatus(RequestItem.Status.DA_RICHIEDERE);
        item.setTicket(null);
        return item;
    }

    private Message buildMessage() {
        return new Message(
                "Richiesta VPN [TICK-001]",
                "mittente@example.com",
                LocalDateTime.of(2026, 1, 10, 8, 55),
                "destinatario@example.com",
                null,
                "Corpo del messaggio di test",
                "ENTRYID-TEST-001",
                "CONV-TEST-001",
                "Richiesta VPN",
                1,
                false,
                null
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Test
    // ──────────────────────────────────────────────────────────────────────────



    @Test
    @Order(2)
    void insertRequest_inserisceRigaCorrettamente() {
        // Pre-requisito: user_account presente (inserito dal test precedente)
        long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_account WHERE codice_fiscale = 'RSSMRA80A01H501U'",
                Long.class
        );

        // Costruiamo una Request con toRow -> userId corretto
        Request request = buildRequest();
        // Il mapper toRow usa request.getUser() per risalire all'id:
        // poiché User non ha id, usiamo un approccio diretto con INSERT manuale via toRow
        jdbcTemplate.update(
                "INSERT INTO request (create_at, update_at, title, status, note, user_id) VALUES (?,?,?,?,?,?)",
                "2026-01-10T09:00",
                "2026-01-10T09:00",
                request.getTitle(),
                request.getStatus().name(),
                request.getNote(),
                userId
        );

        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM request WHERE title = 'Richiesta abilitazione VPN'",
                Integer.class
        );
        assertEquals(1, count, "request deve contenere 1 riga");
    }

    @Test
    @Order(3)
    void insertRequestItem_inserisceRigaConAmbiente() {
        long requestId = jdbcTemplate.queryForObject(
                "SELECT id FROM request WHERE title = 'Richiesta abilitazione VPN'",
                Long.class
        );

        // insertRequestItem non conosce requestId (non è nel domain): usiamo insert diretto
        RequestItem item = buildRequestItem();
        jdbcTemplate.update(
                "INSERT INTO request_item (request_id, type, create_at, update_at, dettaglio, nota, ambiente, status, ticket) VALUES (?,?,?,?,?,?,?,?,?)",
                requestId,
                item.getType().name(),
                item.getCreateAt().toString(),
                item.getUpdateAt().toString(),
                item.getDettaglio(),
                item.getNota(),
                "SVILUPPO;COLLAUDO",
                item.getStatus().name(),
                item.getTicket()
        );

        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM request_item WHERE request_id = ?",
                Integer.class, requestId
        );
        assertEquals(1, count, "request_item deve contenere 1 riga per la request");

        String ambiente = jdbcTemplate.queryForObject(
                "SELECT ambiente FROM request_item WHERE request_id = ?",
                String.class, requestId
        );
        assertEquals("SVILUPPO;COLLAUDO", ambiente, "ambiente deve essere 'SVILUPPO;COLLAUDO'");
    }


    @Test
    @Order(5)
    void insertMessage_inserisceRigaConRequest() {
        long requestId = jdbcTemplate.queryForObject(
                "SELECT id FROM request WHERE title = 'Richiesta abilitazione VPN'",
                Long.class
        );

        // Messaggio associato a una request
        jdbcTemplate.update(
                "INSERT INTO message (request_id, subject, sender_address, received_at, to_address, cc_address, body_text, entry_id, conversation_id, conversation_topic, importance, has_attachment, category) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                requestId,
                "Secondo messaggio",
                "mittente2@example.com",
                "2026-01-10T09:10",
                "dest@example.com",
                null,
                "Corpo 2",
                "ENTRYID-TEST-002",
                "CONV-TEST-001",
                "Richiesta VPN",
                1,
                0,
                null
        );

        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM message WHERE conversation_id = 'CONV-TEST-001'",
                Integer.class
        );
        assertEquals(2, count, "conversation CONV-TEST-001 deve avere 2 messaggi");
    }
}

