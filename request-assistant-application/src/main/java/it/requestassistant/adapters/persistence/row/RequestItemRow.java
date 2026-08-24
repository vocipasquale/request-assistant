package it.requestassistant.adapters.persistence.row;

import java.time.LocalDateTime;
import java.util.List;

public record RequestItemRow(
        long id,
        Long requestId,
        String type,
        LocalDateTime createAt,
        LocalDateTime updateAt,
        String dettaglio,
        String nota,
        String ambienteRaw,        // es: "SVILUPPO;COLLAUDO"
        String status,
        String ticket
) {
    public List<String> ambienteList() {
        return it.requestassistant.adapters.persistence.util.PersistenceConverters.splitSemicolon(ambienteRaw);
    }
}
