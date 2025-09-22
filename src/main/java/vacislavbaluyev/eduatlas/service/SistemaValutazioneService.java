package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaValutazione;
import vacislavbaluyev.eduatlas.entities.TipoVoto;
import vacislavbaluyev.eduatlas.exception.ResourceNotFoundException;
import vacislavbaluyev.eduatlas.payload.SistemaValutazioneCreateDTO;
import vacislavbaluyev.eduatlas.payload.SistemaValutazioneDTO;
import vacislavbaluyev.eduatlas.repository.PaeseRepository;
import vacislavbaluyev.eduatlas.repository.SistemaValutazioneRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class SistemaValutazioneService {

    private final SistemaValutazioneRepository sistemaValutazioneRepository;
    private final PaeseRepository paeseRepository;

    public SistemaValutazioneService(SistemaValutazioneRepository sistemaValutazioneRepository,
                                     PaeseRepository paeseRepository) {
        this.sistemaValutazioneRepository = sistemaValutazioneRepository;
        this.paeseRepository = paeseRepository;
    }

    /**
     * Recupera tutti i sistemi di valutazione
     */
    public List<SistemaValutazioneDTO> getAllSistemiValutazione() {
        return sistemaValutazioneRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recupera un sistema di valutazione per ID
     */
    public SistemaValutazioneDTO getSistemaValutazioneById(Long id) {
        SistemaValutazione sistema = sistemaValutazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema Valutazione", "id", id));
        return convertToDTO(sistema);
    }

    /**
     * Recupera il sistema di valutazione per un paese
     */
    public SistemaValutazione getSistemaValutazionePerPaese(Paese paese) {
        return sistemaValutazioneRepository.findByPaese(paese)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema Valutazione", "paese", paese.getNome()));
    }

    /**
     * Aggiorna un sistema di valutazione esistente
     */
    public SistemaValutazioneDTO updateSistemaValutazione(Long id, SistemaValutazioneCreateDTO updateDTO) {
        SistemaValutazione sistema = sistemaValutazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema Valutazione", "id", id));

        sistema.setVotoA(updateDTO.votoMassimo());
        sistema.setVotoB(calcolaVotoB(updateDTO.votoMassimo(), updateDTO.votoMinimo()));
        sistema.setVotoC(calcolaVotoC(updateDTO.votoMassimo(), updateDTO.votoMinimo()));
        sistema.setVotoDE(updateDTO.votoSufficienza());
        sistema.setVotoF(updateDTO.votoMinimo());
        sistema.setScalaTipo(updateDTO.scalaTipo());

        return convertToDTO(sistemaValutazioneRepository.save(sistema));
    }

    /**
     * Elimina un sistema di valutazione
     */
    public void deleteSistemaValutazione(Long id) {
        SistemaValutazione sistema = sistemaValutazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema Valutazione", "id", id));
        sistemaValutazioneRepository.delete(sistema);
    }

    /**
     * Determina il tipo di voto basato sul voto fornito
     */
    public TipoVoto determinaTipoVoto(String voto, Long paeseId) {
        Paese paese = paeseRepository.findById(paeseId)
                .orElseThrow(() -> new ResourceNotFoundException("Paese", "id", paeseId));

        SistemaValutazione sistema = getSistemaValutazionePerPaese(paese);

        if (voto == null || voto.trim().isEmpty()) {
            return null;
        }

        String votoNormalizzato = normalizzaVoto(voto);

        // Confronto diretto
        if (confrontaVoti(votoNormalizzato, sistema.getVotoA())) return TipoVoto.A;
        if (confrontaVoti(votoNormalizzato, sistema.getVotoB())) return TipoVoto.B;
        if (confrontaVoti(votoNormalizzato, sistema.getVotoC())) return TipoVoto.C;
        if (confrontaVoti(votoNormalizzato, sistema.getVotoDE())) return TipoVoto.DE;
        if (confrontaVoti(votoNormalizzato, sistema.getVotoF())) return TipoVoto.F;

        // Prova conversione numerica
        return determinaTipoVotoNumerico(votoNormalizzato, sistema);
    }

    // Metodi di utilità privati

    private String calcolaVotoB(String votoMax, String votoMin) {
        try {
            double max = estraiNumero(votoMax);
            double min = estraiNumero(votoMin);
            double range = max - min;
            double votoB = max - (range / 5.0);
            return String.format("%.1f", votoB);
        } catch (Exception e) {
            return votoMax; // Fallback al voto massimo se non è possibile calcolare
        }
    }

    private String calcolaVotoC(String votoMax, String votoMin) {
        try {
            double max = estraiNumero(votoMax);
            double min = estraiNumero(votoMin);
            double range = max - min;
            double votoC = max - (2 * range / 5.0);
            return String.format("%.1f", votoC);
        } catch (Exception e) {
            return votoMax; // Fallback al voto massimo se non è possibile calcolare
        }
    }

    private String normalizzaVoto(String voto) {
        return voto.trim().toUpperCase();
    }

    private boolean confrontaVoti(String voto1, String voto2) {
        if (voto1 == null || voto2 == null) return false;
        return voto1.equals(voto2) ||
                voto1.contains(voto2) ||
                voto2.contains(voto1);
    }

    private TipoVoto determinaTipoVotoNumerico(String voto, SistemaValutazione sistema) {
        try {
            double votoNumerico = estraiNumero(voto);
            double votoMax = estraiNumero(sistema.getVotoA());
            double votoMin = estraiNumero(sistema.getVotoF());

            if (Double.isNaN(votoNumerico) || Double.isNaN(votoMax) || Double.isNaN(votoMin)) {
                return null;
            }

            double range = votoMax - votoMin;
            double step = range / 5.0;

            if (votoNumerico >= votoMax - step) return TipoVoto.A;
            if (votoNumerico >= votoMax - (2 * step)) return TipoVoto.B;
            if (votoNumerico >= votoMax - (3 * step)) return TipoVoto.C;
            if (votoNumerico >= votoMax - (4 * step)) return TipoVoto.DE;
            return TipoVoto.F;

        } catch (Exception e) {
            log.warn("Errore nella conversione numerica del voto: {}", voto);
            return null;
        }
    }

    private double estraiNumero(String voto) {
        try {
            String numerico = voto.replaceAll("[^0-9.]", "");
            return Double.parseDouble(numerico);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private SistemaValutazioneDTO convertToDTO(SistemaValutazione sistema) {
        return new SistemaValutazioneDTO(
                sistema.getId(),
                sistema.getPaese().getId(),
                sistema.getPaese().getNome(),
                sistema.getVotoA(),
                sistema.getVotoB(),
                sistema.getVotoC(),
                sistema.getVotoDE(),
                sistema.getVotoF(),
                sistema.getScalaTipo()
        );
    }
}