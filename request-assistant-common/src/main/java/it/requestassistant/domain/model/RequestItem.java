package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class RequestItem {
    private long id;
    private Type type;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String dettaglio;
    private String nota;
    private List<Ambiente> ambiente;
    private Status status;
    private String ticket;

    public enum Type {
        NUOVA_UTENZA,
        DOMINIO_APN_VPN;
    }

    public enum Ambiente {
        SVILUPPO,
        COLLAUDO,
        CERTIFICAZIONE,
        FUSIONE,
        PRODUZIONE
    }

    public enum Status {
        DA_RICHIEDERE,
        RICHIESTO,
        SOLLECITATO,
        IN_ATTESA_RISCONTRO_UTENTE,
        RISCONTRO_OK,
        RISCONTRO_KO;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public String getDettaglio() {
        return dettaglio;
    }

    public void setDettaglio(String dettaglio) {
        this.dettaglio = dettaglio;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public List<Ambiente> getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(List<Ambiente> ambiente) {
        this.ambiente = ambiente;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

}
