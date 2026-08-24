# Conversation
Implementare una classe Conversation anzichè avere List<Message> in Request?

# Eventi Spring

Spring permette di pubblicare eventi applicativi:

applicationEventPublisher.publishEvent(
new LavorazioneCompletataEvent(lavorazione)
);

@Component
public class DashboardEventListener {

    @EventListener
    public void onLavorazioneCompletata(
            LavorazioneCompletataEvent event) {

    }
}