package vacislavbaluyev.eduatlas.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vacislavbaluyev.eduatlas.entities.ScalaTipoEnum;

public record SistemaValutazioneCreateDTO(  @NotBlank(message = "Il voto massimo è obbligatorio")
                                            String votoMassimo,

                                            @NotBlank(message = "Il voto minimo è obbligatorio")
                                            String votoMinimo,

                                            @NotBlank(message = "Il voto di sufficienza è obbligatorio")
                                            String votoSufficienza,

                                            @NotNull(message = "Il tipo di scala è obbligatorio")
                                            ScalaTipoEnum scalaTipo

) {
}
