package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacislavbaluyev.eduatlas.entities.*;
import vacislavbaluyev.eduatlas.exception.CsvImportException;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.SistemaUniversitarioRepository;
import vacislavbaluyev.eduatlas.repository.SistemaValutazioneRepository;
import vacislavbaluyev.eduatlas.repository.TitoloStudioRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Service
public class CsvImportService {
    private static final int EXPECTED_COLUMNS = 19;  // Corretto a 19 colonne
    
    private final PaeseRepository paeseRepository;
    private final SistemaUniversitarioRepository sistemaUniversitarioRepository;
    private final SistemaValutazioneRepository sistemaValutazioneRepository;
    private final TitoloStudioRepository titoloStudioRepository;

    public CsvImportService(PaeseRepository paeseRepository,
                            SistemaUniversitarioRepository sistemaUniversitarioRepository,
                            SistemaValutazioneRepository sistemaValutazioneRepository, TitoloStudioRepository titoloStudioRepository) {
        this.paeseRepository = paeseRepository;
        this.sistemaUniversitarioRepository = sistemaUniversitarioRepository;
        this.sistemaValutazioneRepository = sistemaValutazioneRepository;
        this.titoloStudioRepository = titoloStudioRepository;
    }

    @Transactional
    public void importCsvData(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // Salta le prime due righe (header)
            br.readLine(); // Salta la prima riga di intestazione
            br.readLine(); // Salta la seconda riga di intestazione

            String line;
            int lineNumber = 3; // Iniziamo da 3 perché abbiamo saltato due righe

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        String[] values = parseCsvLine(line);
                        validateLine(values, lineNumber);
                        processCSVLine(values);
                        log.debug("Riga {} processata con successo", lineNumber);
                    } catch (Exception e) {
                        log.error("Errore alla riga {}: {}", lineNumber, e.getMessage());
                        throw new CsvImportException("Errore alla riga " + lineNumber + ": " + e.getMessage());
                    }
                }
                lineNumber++;
            }

            log.info("Importazione completata con successo");
        }
    }


    private void validateLine(String[] values, int lineNumber) {
        if (values.length != EXPECTED_COLUMNS) {
            throw new CsvImportException(String.format(
                "Riga %d: numero di colonne errato. Attese %d colonne, trovate %d",
                lineNumber, EXPECTED_COLUMNS, values.length));
        }

        // Validazione dei valori obbligatori
        if (values[0].trim().isEmpty()) {
            throw new CsvImportException(String.format(
                "Riga %d: Nome paese mancante", lineNumber));
        }
    }

    private String[] parseCsvLine(String line) {
        String[] allValues = line.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        // Prendiamo solo le prime 19 colonne
        String[] values = Arrays.copyOf(allValues, EXPECTED_COLUMNS);
        return Arrays.stream(values)
                .map(this::cleanValue)
                .toArray(String[]::new);
    }


    private void processCSVLine(String[] values) {
        try {
            Paese paese = creaPaese(values);
            creaSistemaValutazione(values, paese);
            creaSistemaUniversitario(values, paese);
            creaTitoloStudio(values,paese);
            log.debug("Importati dati completi per il paese: {}", paese.getNome());
        } catch (Exception e) {
            throw new CsvImportException("Errore nell'elaborazione dei dati per il paese " + 
                    values[0] + ": " + e.getMessage());
        }
    }

    private void creaTitoloStudio(String[] values, Paese paese){

        String denominazione = values[18].trim();

        if (!denominazione.isEmpty()){
            TitoloStudio titoloStudio = TitoloStudio.builder()
                    .paese(paese)
                    .denominazione(denominazione)
                    .build();

            titoloStudioRepository.save(titoloStudio);
            log.debug("Creato titolo di studio: {} per il paese: {}",
                    denominazione, paese.getNome());

        }
    }

    private Paese creaPaese(String[] values) {
        String nomePaese = values[0].trim();
        if (nomePaese.isEmpty()) {
            throw new CsvImportException("Nome paese non può essere vuoto");
        }

        Integer anniScuola = paeseAnniScuola(values[1]);

        return paeseRepository.save(Paese.builder()
                .nome(nomePaese)
                .anniSculaObbligaroia(anniScuola)
                .build());
    }

    private void creaSistemaValutazione(String[] values, Paese paese) {
        String votoA = values[12].trim();
        String votoB = values[13].trim();
        String votoC = values[14].trim();
        String votoDE = values[15].trim();
        String votoF = values[16].trim();

        if (!votoA.isEmpty() || !votoB.isEmpty() || !votoC.isEmpty() || 
            !votoDE.isEmpty() || !votoF.isEmpty()) {
            
            SistemaValutazione sistema = SistemaValutazione.builder()
                    .paese(paese)
                    .votoA(votoA.isEmpty() ? null : votoA)
                    .votoB(votoB.isEmpty() ? null : votoB)
                    .votoC(votoC.isEmpty() ? null : votoC)
                    .votoDE(votoDE.isEmpty() ? null : votoDE)
                    .votoF(votoF.isEmpty() ? null : votoF)
                    .scalaTipo(determinaTipoScala(votoA))
                    .build();
            
            sistemaValutazioneRepository.save(sistema);
        }
    }

    private void creaSistemaUniversitario(String[] values, Paese paese) {
        Integer durataBase = calcolaDurataBase(values);
        Integer creditiPerAnno = calcolaCrediti(values);
        String livelloEQF = values[17].trim();

        if (durataBase != null || creditiPerAnno != null || !livelloEQF.isEmpty()) {
            SistemaUniversitario sistema = SistemaUniversitario.builder()
                    .paese(paese)
                    .durataBaseAnni(durataBase)
                    .creditiPerAnno(creditiPerAnno)
                    .livelloEQF(livelloEQF.isEmpty() ? null : livelloEQF)
                    .build();
            
            sistemaUniversitarioRepository.save(sistema);
        }
    }

    private Integer paeseAnniScuola(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String anni = value.split("\\|")[0].replaceAll("[^0-9]", "");
        return anni.isEmpty() ? null : Integer.parseInt(anni);
    }

    private Integer calcolaDurataBase(String[] values) {
        try {
            return Integer.parseInt(values[4].replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            log.warn("Errore nel parsing della durata base: {}", values[4]);
            return null;
        }
    }

    private Integer calcolaCrediti(String[] values) {
        try {
            if (!values[7].trim().isEmpty()) {
                return Integer.parseInt(values[7].replaceAll("[^0-9]", ""));
            }
        } catch (NumberFormatException e) {
            log.warn("Errore nel parsing dei crediti per anno: {}", values[7]);
        }
        return null;
    }

    private TipoScala determinaTipoScala(String voto) {
        if (voto == null || voto.trim().isEmpty()) return TipoScala.NUMERICO;
        
        String votoNorm = voto.trim().replaceAll("[^A-Za-z0-9.]", "");
        
        if (votoNorm.matches("[A-Za-z]+")) return TipoScala.LETTERE;
        if (votoNorm.matches("[0-9.]+")) return TipoScala.NUMERICO;
        return TipoScala.PERCENTUALE;
    }

    private String cleanValue(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\"\"", "\"");
    }
}