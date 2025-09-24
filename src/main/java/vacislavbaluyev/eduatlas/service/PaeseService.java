package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;
import vacislavbaluyev.eduatlas.entities.SistemaValutazione;
import vacislavbaluyev.eduatlas.exception.ResourceNotFoundException;
import vacislavbaluyev.eduatlas.payload.DettaglioPaeseDTO;
import vacislavbaluyev.eduatlas.payload.PaeseCreateDTO;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.SistemaUniversitarioRepository;
import vacislavbaluyev.eduatlas.repository.SistemaValutazioneRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PaeseService {

    private final PaeseRepository paeseRepository;
    private final SistemaValutazioneRepository sistemaValutazioneRepository;
    private final SistemaUniversitarioRepository sistemaUniversitarioRepository;

    public PaeseService(
            PaeseRepository paeseRepository,
            SistemaValutazioneRepository sistemaValutazioneRepository,
            SistemaUniversitarioRepository sistemaUniversitarioRepository) {
        this.paeseRepository = paeseRepository;
        this.sistemaValutazioneRepository = sistemaValutazioneRepository;
        this.sistemaUniversitarioRepository = sistemaUniversitarioRepository;
    }

    public List<DettaglioPaeseDTO> getAllPaesi() {
        return paeseRepository.findAll().stream()
                .map(paese -> {
                    try {
                        SistemaValutazione sistemaVal = sistemaValutazioneRepository.findByPaese(paese)
                                .orElse(null);
                        SistemaUniversitario sistemaUni = sistemaUniversitarioRepository.findByPaeseId(paese.getId())
                                .orElse(null);
                        if (sistemaVal != null && sistemaUni != null) {
                            return DettaglioPaeseDTO.fromEntities(paese, sistemaVal, sistemaUni);
                        }
                    } catch (Exception e) {
                        log.error("Errore nel recupero dei dati per il paese {}: {}", paese.getNome(), e.getMessage());
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public DettaglioPaeseDTO getPaeseById(Long id) {
        Paese paese = paeseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paese", "id", id));

        SistemaValutazione sistemaVal = sistemaValutazioneRepository.findByPaese(paese)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema Valutazione", "paese", paese.getNome()));

        SistemaUniversitario sistemaUni = sistemaUniversitarioRepository.findByPaeseId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema Universitario", "paese", paese.getNome()));

        return DettaglioPaeseDTO.fromEntities(paese, sistemaVal, sistemaUni);
    }

    public DettaglioPaeseDTO getPaeseByNome(String nome) {
        Paese paese = paeseRepository.findByNome(nome)
                .orElseThrow(() -> new ResourceNotFoundException("Paese", "nome", nome));

        return getPaeseById(paese.getId());
    }

    public List<DettaglioPaeseDTO> getPaesiByAnniScuolaObbligatoria(Integer anni) {
        return paeseRepository.findByAnniScuolaObbligatoria(anni).stream()
                .map(paese -> {
                    try {
                        return getPaeseById(paese.getId());
                    } catch (Exception e) {
                        log.error("Errore nel recupero dei dati per il paese {}: {}", paese.getNome(), e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<DettaglioPaeseDTO> getPaesiByDurataBaseAnni(Integer durataAnni) {
        return sistemaUniversitarioRepository.findByDurataBaseAnni(durataAnni).stream()
                .map(sistema -> {
                    try {
                        return getPaeseById(sistema.getPaese().getId());
                    } catch (Exception e) {
                        log.error("Errore nel recupero dei dati per il sistema universitario: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<DettaglioPaeseDTO> getPaesiByCreditiPerAnno(Integer crediti) {
        return sistemaUniversitarioRepository.findByCreditiPerAnno(crediti).stream()
                .map(sistema -> {
                    try {
                        return getPaeseById(sistema.getPaese().getId());
                    } catch (Exception e) {
                        log.error("Errore nel recupero dei dati per il sistema universitario: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<DettaglioPaeseDTO> getPaesiByLivelloEQF(String livello) {
        return sistemaUniversitarioRepository.findByLivelloEQF(livello).stream()
                .map(sistema -> {
                    try {
                        return getPaeseById(sistema.getPaese().getId());
                    } catch (Exception e) {
                        log.error("Errore nel recupero dei dati per il sistema universitario: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public DettaglioPaeseDTO createPaese(PaeseCreateDTO createDTO) {
        if (paeseRepository.existsByNome(createDTO.nome())) {
            throw new IllegalArgumentException("Un paese con questo nome esiste già");
        }

        Paese paese = Paese.builder()
                .nome(createDTO.nome())
                .anniSculaObbligaroia(createDTO.anniScuolaObbligatoria())
                .build();

        paese = paeseRepository.save(paese);


        return getPaeseById(paese.getId());
    }



    public void deletePaese(Long id) {
        if (!paeseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paese", "id", id);
        }
        paeseRepository.deleteById(id);
    }
}

