package vacislavbaluyev.eduatlas.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;
import vacislavbaluyev.eduatlas.entities.SistemaValutazione;
import vacislavbaluyev.eduatlas.entities.TipoScala;
import vacislavbaluyev.eduatlas.payload.PaeseCompletoCreateDTO;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.SistemaUniversitarioRepository;
import vacislavbaluyev.eduatlas.repository.SistemaValutazioneRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaeseCompletoService {
    private final PaeseRepository paeseRepository;
    private final SistemaUniversitarioRepository sistemaUniversitarioRepository;
    private final SistemaValutazioneRepository sistemaValutazioneRepository;

    @Transactional
    public Paese createPaeseCompleto(PaeseCompletoCreateDTO dto) {
        // Verifica se esiste già un paese con lo stesso nome
        if (paeseRepository.existsByNome(dto.nome())) {
            throw new IllegalArgumentException("Un paese con questo nome esiste già");
        }

        // 1. Crea il paese
        Paese paese = paeseRepository.save(Paese.builder()
                .nome(dto.nome())
                .anniSculaObbligaroia(dto.anniScuolaObbligatoria())
                .build());

        // 2. Crea il sistema di valutazione
        if (dto.votoMassimo() != null || dto.votoMinimo() != null || dto.votoSufficienza() != null) {
            SistemaValutazione sistema = SistemaValutazione.builder()
                    .paese(paese)
                    .votoA(dto.votoMassimo())
                    .votoF(dto.votoMinimo())
                    .votoDE(dto.votoSufficienza())
                    .scalaTipo(dto.scalaTipo())
                    .build();
            sistemaValutazioneRepository.save(sistema);
        }

        // 3. Crea il sistema universitario
        if (dto.durataBaseAnni() != null || dto.creditiPerAnno() != null || dto.livelloEQF() != null) {
            SistemaUniversitario sistema = SistemaUniversitario.builder()
                    .paese(paese)
                    .durataBaseAnni(dto.durataBaseAnni())
                    .creditiPerAnno(dto.creditiPerAnno())
                    .livelloEQF(dto.livelloEQF())
                    .build();
            sistemaUniversitarioRepository.save(sistema);
        }

        return paese;
    }

    @Transactional
    public Paese createPaeseFromCsv(String[] values) {
        // Converte i valori CSV in DTO
        PaeseCompletoCreateDTO dto = convertCsvToPaeseDTO(values);
        return createPaeseCompleto(dto);
    }

    private PaeseCompletoCreateDTO convertCsvToPaeseDTO(String[] values) {
        return new PaeseCompletoCreateDTO(
                values[0].trim(),                              // nome
                paeseAnniScuola(values[1]),                   // anniScuolaObbligatoria
                values[12].trim(),                            // votoMassimo (A)
                values[16].trim(),                            // votoMinimo (F)
                values[15].trim(),                            // votoSufficienza (DE)
                TipoScala.PERCENTUALE,                        // scalaTipo
                calcolaDurataBase(values),                    // durataBaseAnni
                calcolaCrediti(values),                       // creditiPerAnno
                values[17].trim()                             // livelloEQF
        );
    }

    // Metodi di utilità per il parsing dei valori CSV
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
}