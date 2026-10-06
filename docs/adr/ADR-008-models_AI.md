# ADR-008 – Modelli AI

## Status

Accepted

## Requisiti generali

Dopo svariate prove su diversi modelli mi sono reso conto che con l'hardware a disposizione (DELL i5 no GPU) risulta monto "faticoso" per un unico modello AI portare a termine le analisi senza sbagliare. Per questo motivo ho deciso di utilizzare più modelli AI in sequenza, in modo da poter suddividere il carico elaborativo.
Pertanto ho deciso di suddividere l'analisi in tre fasi distinte e assegnare ogni fase a un medello specifico:
1. **Fase 1**: In presenza di allegati nella mail in arrivo, inviare i file allegato a un modello "Visual" per ottenere un riassunto del contenuto (modello AI: `request-assistant-vision-v1`)
2. **Fase 2**: Classificazione del messaggio in arrivo, arricchito con eventuale contesto degli allegati, ed eventuale Request già presente nel DB. Le uniche risposte che il modello AI può dare sono: "NUOVA_RICHIESTA", "MODIFICA_RICHIESTA", "RISPONDI_A_MAIL". (modello AI: `request-assistant-classificatore-v7`, [../ollama/model-definitions/request-assistant/classificatore/Modelfile-v7.txt])
3. **Fase 3**: In base alla risposta ottenuta nella fase 2, un altro modello in base all'invio di tutto il contesto fin'ora costruito, restituirà: una bozza di mail di risposta ("RISPONDI_A_MAIL"), oppure una bozza di richiesta da inserire nel DB ("NUOVA_RICHIESTA"), oppure una bozza di richiesta modificata rispoetto all'input inviato ("MODIFICA_RICHIESTA"). (modello AI: `Qwen 8B`)
