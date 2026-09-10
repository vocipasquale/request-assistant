package it.requestassistant.domain.model;

public class User {
    private long id;
    private String cognome;
    private String nome;
    private String codiceFiscale;
    private String email;
    private String utenza;

    public User(){}

    public User(long id, String cognome, String nome, String utenza) {
        this.id = id;
        this.cognome = cognome;
        this.nome = nome;
        this.utenza = utenza;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUtenza() {
        return utenza;
    }

    public void setUtenza(String utenza) {
        this.utenza = utenza;
    }
}
