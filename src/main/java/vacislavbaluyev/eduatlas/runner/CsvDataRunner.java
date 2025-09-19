package vacislavbaluyev.eduatlas.runner;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vacislavbaluyev.eduatlas.entities.NotaPaese;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;
import vacislavbaluyev.eduatlas.repository.NotaPaeseRepository;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.SistemaUniversitarioRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CsvDataRunner implements CommandLineRunner {

    private final PaeseRepository paeseRepository;
    private final SistemaUniversitarioRepository sistemaUniversitarioRepository;
    private final NotaPaeseRepository notaPaeseRepository;
    private static final String CSV_FILE_PATH = "MATRIXCSV.csv";

    @Autowired
    public CsvDataRunner(PaeseRepository paeseRepository,
                         SistemaUniversitarioRepository sistemaUniversitarioRepository,
                         NotaPaeseRepository notaPaeseRepository) { // Aggiungi al costruttore
        this.paeseRepository = paeseRepository;
        this.sistemaUniversitarioRepository = sistemaUniversitarioRepository;
        this.notaPaeseRepository = notaPaeseRepository;
    }


    @Override
    @Transactional
    public void run(String... args) {
        try {
            if (paeseRepository.count() > 0) {
                log.info("Il database contiene già dei dati. Saltando l'importazione CSV.");
                return;
            }

            log.info("Iniziando l'importazione dei dati dal CSV...");
            importCSVData();
            log.info("Importazione CSV completata con successo.");

        } catch (Exception e) {
            log.error("Errore durante l'importazione del CSV: {}", e.getMessage(), e);
        }
    }

    private void importCSVData() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream(CSV_FILE_PATH),
                        StandardCharsets.UTF_8))) {

            // Salta le prime due righe (intestazioni)
            br.readLine();
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith(";")) {
                    continue;
                }
                processCSVLine(line);
            }

        } catch (IOException e) {
            log.error("Errore nella lettura del file CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nell'importazione del CSV", e);
        }
    }

    private void processCSVLine(String line) {
        String[] values = line.split(";");
        // Verifica che ci siano abbastanza colonne
        if (values.length < 17) {
            log.warn("Riga CSV non valida (colonne insufficienti): {}", line);
            return;
        }

        try {
            // Elabora il nome del paese (rimuovi spazi extra)
            String nomePaese = values[0].trim();

            // Elabora gli anni di scuola obbligatoria
            Integer anniScuola = parseAnniScuola(values[1]);

            // Crea e salva l'entità Paese
            Paese paese = Paese.builder()
                    .nome(nomePaese)
                    .anniSculaObbligaroia(anniScuola)
                    .build();
            paese = paeseRepository.save(paese);

            // Elabora i dati del sistema universitario
            Integer durataBaseAnni = calcolaDurataBase(values);
            Integer creditiPerAnno = parseCrediti(values[7]);
            String livelloEQF = values.length > 16 ? values[16].trim() : null;

            // Crea e salva il sistema universitario
            SistemaUniversitario sistema = SistemaUniversitario.builder()
                    .paese(paese)
                    .durataBaseAnni(durataBaseAnni)
                    .creditiPerAnno(creditiPerAnno)
                    .livelloEQF(livelloEQF)
                    .build();
            sistemaUniversitarioRepository.save(sistema);

            processaNotePerPaese(values, paese);

            log.debug("Importati dati per il paese: {}", nomePaese);

        } catch (Exception e) {
            log.error("Errore nell'elaborazione della riga CSV: {}", line, e);
        }
    }



    private void processaNotePerPaese(String[] values, Paese paese) {
        for (int i = 0; i < values.length; i++) {
            String value = values[i].trim();
            if (value.contains("*")) {
                for (int j = i + 1; j < values.length; j++) {
                    String potentialNote = values[j].trim();
                    if (potentialNote.startsWith("*") || potentialNote.startsWith("(*)")) {
                        NotaPaese nota = NotaPaese.builder()
                                .paese(paese)
                                .nota(potentialNote.replaceAll("^[*(]\\*[)]*", "").trim())
                                .colonnaRifermineto(i)
                                .build();
                        notaPaeseRepository.save(nota); // Usa l'istanza iniettata
                        log.debug("Salvata nota per il paese {} colonna {}: {}",
                                paese.getNome(), i, nota.getNota());
                        break;
                    }
                }
            }
        }
    }



    private Integer parseAnniScuola(String value) {
        try {
            // Gestisce casi come "12" o "12|13*"
            String[] parts = value.split("\\|");
            String anni = parts[0].replaceAll("[^0-9]", "");
            return Integer.parseInt(anni);
        } catch (Exception e) {
            log.warn("Errore nel parsing degli anni di scuola: {}", value);
            return null;
        }
    }

    private Integer calcolaDurataBase(String[] values) {
        try {
            // Cerca il primo valore non vuoto tra gli anni di durata (indici 2-4)
            for (int i = 2; i <= 4; i++) {
                if (!values[i].trim().isEmpty()) {
                    String durataStr = values[i].replaceAll("[^0-9]", "");
                    return Integer.parseInt(durataStr);
                }
            }
        } catch (Exception e) {
            log.warn("Errore nel calcolo della durata base");
        }
        return null;
    }

    private Integer parseCrediti(String value) {
        try {
            String crediti = value.replaceAll("[^0-9]", "");
            if (!crediti.isEmpty()) {
                return Integer.parseInt(crediti);
            }
        } catch (Exception e) {
            log.warn("Errore nel parsing dei crediti: {}", value);
        }
        return null;
    }
}