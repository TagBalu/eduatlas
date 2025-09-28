package vacislavbaluyev.eduatlas.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vacislavbaluyev.eduatlas.entities.TipoScala;

public record PaeseCreateDTO(
        @NotBlank(message = "Il nome del paese è obbligatorio")
        String nome,

        @NotNull(message = "Gli anni di scuola obbligatoria sono obbligatori")
        @Min(value = 1, message = "Gli anni di scuola obbligatoria devono essere almeno 1")
        @Max(value = 20, message = "Gli anni di scuola obbligatoria non possono superare 20")
        Integer anniScuolaObbligatoria,

        @NotBlank(message = "Il voto A è obbligatorio")
        String votoA,

        @NotBlank(message = "Il voto B è obbligatorio")
        String votoB,

        @NotBlank(message = "Il voto C è obbligatorio")
        String votoC,

        @NotBlank(message = "Il voto DE è obbligatorio")
        String votoDE,

        @NotBlank(message = "Il voto F è obbligatorio")
        String votoF,

        @NotNull(message = "Il tipo di scala è obbligatorio")
        TipoScala scalaTipo,

        @NotNull(message = "La durata base in anni è obbligatoria")
        @Min(value = 1, message = "La durata base deve essere almeno 1 anno")
        @Max(value = 10, message = "La durata base non può superare 10 anni")
        Integer durataBaseanni,

        @NotNull(message = "I crediti per anno sono obbligatori")
        @Min(value = 1, message = "I crediti per anno devono essere almeno 1")
        @Max(value = 100, message = "I crediti per anno non possono superare 100")
        Integer creditiPerAnno,

        @NotBlank(message = "Il livello EQF è obbligatorio")
        String livelloEQF
) {}