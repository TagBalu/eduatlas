package vacislavbaluyev.eduatlas.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaeseCreateDTO(
        @NotBlank(message = "Il nome del paese è obbligatorio")
        String nome,

        @NotNull(message = "Gli anni di scuola obbligatoria sono obbligatori")
        int anniScuolaObbligatoria
) {
}
