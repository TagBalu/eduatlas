package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacislavbaluyev.eduatlas.entities.*;
import vacislavbaluyev.eduatlas.exception.CsvImportException;
import vacislavbaluyev.eduatlas.repository.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
@Slf4j
public class CsvImportService{
    private static final int EXPECTED_COLUMNS = 19;
    
    private final PaeseRepository paeseRepository;
    private final SistemaUniversitarioRepository sistemaUniversitarioRepository;
    private final SistemaValutazioneRepository sistemaValutazioneRepository;

    public CsvImportService(PaeseRepository paeseRepository,
                           SistemaUniversitarioRepository sistemaUniversitarioRepository,
                           SistemaValutazioneRepository sistemaValutazioneRepository) {
        this.paeseRepository = paeseRepository;
        this.sistemaUniversitarioRepository = sistemaUniversitarioRepository;
        this.sistemaValutazioneRepository = sistemaValutazioneRepository;
    }


    @Transactional
    public void importCsvData(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            // Valida l'header del CSV
            validateHeader(br.readLine(), br.readLine());
            
            String line;
            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        processCSVLine(line);
                    } catch (Exception e) {
                        throw new CsvImportException("Errore alla riga " + lineNumber + ": " + e.getMessage());
                    }
                }
                lineNumber++;
            }
        }
    }


    private void validateHeader(String headerLine1, String headerLine2) {
        if (headerLine1 == null || headerLine2 == null) {
            throw new CsvImportException("Header CSV mancante");
        }
    }

    private void processCSVLine(String line) {
        String[] values = parseCsvLine(line);
        validateValues(values);

        try {
            Paese paese = creaPaese(values);
            creaSistemaValutazione(values, paese);
            creaSistemaUniversitario(values, paese);
            log.debug("Importati dati completi per il paese: {}", paese.getNome());
        } catch (Exception e) {
            throw new CsvImportException("Errore nell'elaborazione dei dati per il paese " + 
                    values[0] + ": " + e.getMessage());
        }
    }

    private String[] parseCsvLine(String line) {
        String[] values = line.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        return Arrays.stream(values)
                .map(this::cleanValue)
                .toArray(String[]::new);
    }

    private void validateValues(String[] values) {
        if (values.length < EXPECTED_COLUMNS) {
            throw new CsvImportException(
                    String.format("Numero di colonne non valido. Attese: %d, Trovate: %d", 
                            EXPECTED_COLUMNS, values.length));
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
        // Calcola la durata base considerando i primi 3 anni (colonne 2-4)
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
        return TipoScala.DESCRITTIVO;
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