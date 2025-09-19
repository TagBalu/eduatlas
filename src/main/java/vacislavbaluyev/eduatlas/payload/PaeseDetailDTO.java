package vacislavbaluyev.eduatlas.payload;

import java.util.List;

public record PaeseDetailDTO(
        Long id,
        String nome,
        Integer anniScuolaObbligatoria,
        List<SistemaValutazioneDTO> sistemaValutazione
) {
}
