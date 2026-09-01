package it.requestassistant.domain.model;

public class User {
    private String Cognome;
    private String Nome;
    private String codiceFiscale;
    private String email;
    private String utenza;

    public User(){}

    public User(String cognome, String nome, String utenza) {
        Cognome = cognome;
        Nome = nome;
        this.utenza = utenza;
    }

    public String getCognome() {
        return Cognome;
    }

    public void setCognome(String cognome) {
        Cognome = cognome;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        Nome = nome;
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
