package vacislavbaluyev.eduatlas.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vacislavbaluyev.eduatlas.entities.Ruolo;
import vacislavbaluyev.eduatlas.entities.Utente;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.UtenteRepository;
import vacislavbaluyev.eduatlas.service.CsvImportService;

import java.io.IOException;
import java.io.InputStream;


@Component
@Slf4j
public class CsvDataRunner implements CommandLineRunner {
    private static final String CSV_FILE_PATH = "MATRIXCSV.csv";

    private final PaeseRepository paeseRepository;
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final CsvImportService csvImportService;

    @Autowired
    public CsvDataRunner(PaeseRepository paeseRepository,
                        UtenteRepository utenteRepository,
                        PasswordEncoder passwordEncoder,
                        CsvImportService csvImportService) {
        this.paeseRepository = paeseRepository;
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.csvImportService = csvImportService;
    }

    @Override
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
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(CSV_FILE_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("File CSV non trovato: " + CSV_FILE_PATH);
            }
            csvImportService.importCsvData(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'importazione del CSV", e);
        }
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
                    .cognome("Principal")
                    .avatarUrl("https://ui-avatars.com/api/?name=Admin")
                    .ruolo(Ruolo.ROOT_ADMIN)
                    .build();

            utenteRepository.save(adminRoot);
            log.info("Utente admin root creato con successo");
        } else {
            log.info("Utenti già presenti nel sistema");
        }
    }
}