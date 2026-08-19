package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class RequestItem {
    private Type type;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String dettaglio;
    private String nota;
    private List<Ambiente> ambiente;
    private Status status;
    private String ticket;

    private enum Type {
        NUOVA_UTENZA,
        DOMINIO_APN_VPN;
    }

    private enum Ambiente {
        SVILUPPO,
        COLLAUDO,
        CERTIFICAZIONE,
        FUSIONE,
        PRODUZIONE
    }

    private enum Status {
        DA_RICHIEDERE,
        RICHIESTO,
        SOLLECITATO,
        IN_ATTESA_RISCONTRO_UTENTE,
        RISCONTRO_OK,
        RISCONTRO_KO;
    }

}
