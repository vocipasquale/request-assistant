package it.requestassistant.domain.model;

import java.util.List;

public class Action{
    private long id;
    private Title title;
    private List<String> steps;

    public Action (){}

    public Action(Title title, List<String> steps) {
        this.title = title;
        this.steps = steps;
    }

    public Action(long id, Title title, List<String> steps) {
        this.id = id;
        this.title = title;
        this.steps = steps;
    }

    public enum Title {
        INVIA_MAIL("Invia mail"),
        NUOVA_RICHIESTA("Nuova richiesta"),
        MODIFICA_RICHIESTA("Modifica richiesta"),
        CHIUDI_RICHIESTA("Chiudi richiesta");

        private final String title;

        Title(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public static Title fromTitle(String title) {
            for (Title t : values()) {
                if (t.title.equalsIgnoreCase(title)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Tipo azione non supportato: " + title);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}
