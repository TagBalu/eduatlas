package vacislavbaluyev.eduatlas.payload;

import vacislavbaluyev.eduatlas.entities.ScalaTipoEnum;

public record SistemaValutazioneDTO(Long id,
                                    Long paeseId,
                                    String nomePaese,
                                    String votoMassimo,
                                    String votoMinimo,
                                    String votoSufficienza,
                                    ScalaTipoEnum scalaTipoEnum
                                    ) {
}
