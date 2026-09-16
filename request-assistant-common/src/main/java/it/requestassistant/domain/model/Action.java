package it.requestassistant.domain.model;

public class Action{
    private long id;
    private Title title;
    private String aiResponse;

    public Action (){}

    public Action(String action) {
        this.title = Title.fromName(action);
    }

    public Action(Title title, String aiResponse) {
        this.title = title;
        this.aiResponse = aiResponse;
    }

    public Action(long id, Title title, String aiResponse) {
        this.id = id;
        this.title = title;
        this.aiResponse = aiResponse;
    }

    public enum Title {
        RISPONDI_A_MAIL("Rispondi a mail"),
        INOLTRA_MAIL("Inoltra mail"),
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

        public static Title fromName(String name) {
            for (Title t : values()) {
                if (t.name().equalsIgnoreCase(name)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Tipo azione non supportato: " + name);
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

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponce(String aiResponse) {
        this.aiResponse = aiResponse;
    }
}
