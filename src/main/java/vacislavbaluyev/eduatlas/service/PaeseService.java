package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;
import vacislavbaluyev.eduatlas.entities.SistemaValutazione;
import vacislavbaluyev.eduatlas.exception.ResourceNotFoundException;
import vacislavbaluyev.eduatlas.payload.DettaglioPaeseDTO;
import vacislavbaluyev.eduatlas.payload.PaeseCompletoCreateDTO;
import vacislavbaluyev.eduatlas.payload.PaeseCreateDTO;
import vacislavbaluyev.eduatlas.payload.PaeseDTO;
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
    private final PaeseCompletoService paeseCompletoService;

    public PaeseService(
            PaeseRepository paeseRepository,
            SistemaValutazioneRepository sistemaValutazioneRepository,
            SistemaUniversitarioRepository sistemaUniversitarioRepository, PaeseCompletoService paeseCompletoService) {
        this.paeseRepository = paeseRepository;
        this.sistemaValutazioneRepository = sistemaValutazioneRepository;
        this.sistemaUniversitarioRepository = sistemaUniversitarioRepository;
        this.paeseCompletoService = paeseCompletoService;
    }

    public List<DettaglioPaeseDTO> getAllPaesi() {
        return paeseRepository.findAll().stream()
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

    public List<PaeseDTO> searchPaesiByNome(String query) {
        return paeseRepository.findByNomeContainingIgnoreCase(query).stream()
                .map(p -> new PaeseDTO(p.getId(), p.getNome(), p.getAnniSculaObbligaroia()))
                .collect(Collectors.toList());
    }

    public List<DettaglioPaeseDTO> confrontaPaesiByNome(String nome1, String nome2) {
        DettaglioPaeseDTO p1 = getPaeseByNome(nome1);
        DettaglioPaeseDTO p2 = getPaeseByNome(nome2);
        return List.of(p1, p2);
    }
    @Transactional
    public DettaglioPaeseDTO createPaese(PaeseCreateDTO createDTO) {
        // Verifica se esiste già un paese con lo stesso nome
        if (paeseRepository.existsByNome(createDTO.nome())) {
            throw new IllegalArgumentException("Un paese con questo nome esiste già");
        }

        // 1. Crea il paese
        Paese paese = paeseRepository.save(Paese.builder()
                .nome(createDTO.nome())
                .anniSculaObbligaroia(createDTO.anniScuolaObbligatoria())
                .build());

        // 2. Crea il sistema di valutazione
        SistemaValutazione sistemaValutazione = SistemaValutazione.builder()
                .paese(paese)
                .votoA(createDTO.votoA())
                .votoB(createDTO.votoB())
                .votoC(createDTO.votoC())
                .votoDE(createDTO.votoDE())
                .votoF(createDTO.votoF())
                .scalaTipo(createDTO.scalaTipo())
                .build();
        sistemaValutazioneRepository.save(sistemaValutazione);

        // 3. Crea il sistema universitario
        SistemaUniversitario sistemaUniversitario = SistemaUniversitario.builder()
                .paese(paese)
                .durataBaseAnni(createDTO.durataBaseanni())
                .creditiPerAnno(createDTO.creditiPerAnno())
                .livelloEQF(createDTO.livelloEQF())
                .build();
        sistemaUniversitarioRepository.save(sistemaUniversitario);

        // Restituisce il DTO completo
        return DettaglioPaeseDTO.fromEntities(
                paese,
                sistemaValutazione,
                sistemaUniversitario
        );
    }
    @Transactional
    public void deletePaese(Long id) {
        if (!paeseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paese", "id", id);
        }
        paeseRepository.deleteById(id);
    }
}

