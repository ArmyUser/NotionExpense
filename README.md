# Spese S22

App Android privata per registrare velocemente una spesa e sincronizzarla su un
database Notion esistente.

## Cosa fa
- flusso a passi: importo → descrizione → data → conto → categoria
- tastierino numerico con feedback aptico
- tema scuro
- conti e categorie letti dal proprio workspace Notion, con cache offline
- invio in background con WorkManager: la spesa parte anche senza rete e viene
  ritentata finché Notion la accetta
- notifica di esito solo quando Notion ha davvero salvato (o ha fallito
  definitivamente)

## Requisiti
- Android Studio, JDK 17
- `minSdk 26`, `compileSdk 35`
- un database Notion già esistente con una tabella spese

## Setup dopo il clone

Il progetto compila anche senza configurazione: in quel caso l'app funziona ma
non sincronizza nulla. Per collegarlo al proprio Notion:

1. Creare un'integrazione interna su <https://www.notion.so/my-integrations>
   con la sola capability **Insert content**, e copiarne il token.
2. Condividere il proprio database con l'integrazione:
   **⋯ → Connections → Connect to → (la propria integrazione)**.
3. Copiare le chiavi da [`gradle.properties.example`](gradle.properties.example)
   dentro `~/.gradle/gradle.properties` — file che sta **fuori** dal repository
   e non viene mai committato — e valorizzarle.
4. Sincronizzare Gradle e compilare:

   ```bash
   ./gradlew :app:installDebug
   ```

`local.properties` viene rigenerato da Android Studio: non è nel repository
perché contiene il percorso locale dell'SDK.

## Segreti

Nessun token, id o dato privato è presente nel repository. Tutti i valori
arrivano da `~/.gradle/gradle.properties` e finiscono in `BuildConfig` solo a
build time; `BuildConfig` è generato dentro `app/build/`, che è ignorato da Git.
