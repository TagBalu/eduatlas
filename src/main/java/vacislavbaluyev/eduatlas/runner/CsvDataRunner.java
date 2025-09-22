package vacislavbaluyev.eduatlas.runner;


import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;
import vacislavbaluyev.eduatlas.entities.Utente;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.SistemaUniversitarioRepository;
import vacislavbaluyev.eduatlas.repository.UtenteRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@Slf4j
public class CsvDataRunner implements CommandLineRunner {

    private final PaeseRepository paeseRepository;
    private final SistemaUniversitarioRepository sistemaUniversitarioRepository;
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String CSV_FILE_PATH = "MATRIXCSV.csv";

    @Autowired
    public CsvDataRunner(PaeseRepository paeseRepository,
                         SistemaUniversitarioRepository sistemaUniversitarioRepository,
                         UtenteRepository utenteRepository,
                         PasswordEncoder passwordEncoder) {
        this.paeseRepository = paeseRepository;
        this.sistemaUniversitarioRepository = sistemaUniversitarioRepository;
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            initializeRootAdmin();

            if (paeseRepository.count() > 0) {
                log.info("Il database contiene già dei dati. Saltando l'importazione CSV.");
                return;
            }

            log.info("Iniziando l'importazione dei dati dal CSV...");
            importCSVData();
            log.info("Importazione CSV completata con successo.");

        } catch (Exception e) {
            log.error("Errore durante l'inizializzazione: {}", e.getMessage(), e);
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
                if (line.trim().isEmpty()) {
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
        String[] values = line.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        values = Arrays.stream(values)
                .map(this::cleanValue)
                .toArray(String[]::new);

        if (values.length < 1 || values[0].trim().isEmpty()) {
            return;
        }

        try {
            String nomePaese = values[0].trim();

            Paese paese = Paese.builder()
                    .nome(nomePaese)
                    .anniSculaObbligaroia(values.length > 1 ? parseAnniScuola(values[1]) : null)
                    .build();
            paese = paeseRepository.save(paese);

            if (values.length > 7) {
                processSistemaUniversitario(values, paese);
            }

            log.debug("Importati dati per il paese: {}", nomePaese);

        } catch (Exception e) {
            log.error("Errore nell'elaborazione della riga CSV per il paese {}: {}",
                    values[0], e.getMessage());
        }
    }

    private void processSistemaUniversitario(String[] values, Paese paese) {
        Integer durataBaseAnni = calcolaDurataBase(values);
        Integer creditiPerAnno = parseCrediti(values[7]);
        String livelloEQF = values.length > 16 ? values[16].trim() : null;

        SistemaUniversitario sistema = SistemaUniversitario.builder()
                .paese(paese)
                .durataBaseAnni(durataBaseAnni)
                .creditiPerAnno(creditiPerAnno)
                .livelloEQF(livelloEQF)
                .build();
        sistemaUniversitarioRepository.save(sistema);
    }

    private String cleanValue(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\"\"", "\"");
    }

    private Integer parseAnniScuola(String value) {
        try {
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
            for (int i = 4; i <= 6; i++) {
                if (values.length > i && !values[i].trim().isEmpty()) {
                    String durataStr = values[i].replaceAll("[^0-9]", "");
                    if (!durataStr.isEmpty()) {
                        int durata = Integer.parseInt(durataStr);
                        if (durata >= 3 && durata <= 5) {
                            return durata;
                        }
                    }
                }
            }
            log.debug("Nessuna durata base valida trovata");
        } catch (Exception e) {
            log.warn("Errore nel calcolo della durata base: {}", e.getMessage());
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

    private void initializeRootAdmin() {
        log.info("Verifico la presenza dell'utente admin root...");
        if (utenteRepository.count() == 0) {
            log.info("Creazione utente admin root...");

            String encodedPassword = passwordEncoder.encode("adminRoot123!");

            Utente adminRoot = Utente.builder()
                    .username("admin")
                    .email("admin@eduatlas.com")
                    .password(encodedPassword)
                    .nome("Admin")
                    .avatarUrl("https://ui-avatars.com/api/?name=Admin")
                    .isRootAdmin(true)
                    .isAdmin(true)
                    .build();

            utenteRepository.save(adminRoot);
            log.info("Utente admin root creato con successo");
        } else {
            log.info("Utenti già presenti nel sistema");
        }
    }
}