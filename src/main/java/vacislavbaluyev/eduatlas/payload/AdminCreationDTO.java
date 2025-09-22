package vacislavbaluyev.eduatlas.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreationDTO(
        @NotBlank(message = "Lo username è obbligatorio")
        @Size(min = 3, max = 20, message = "Lo username deve essere tra 3 e 20 caratteri")
        String username,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 6, max = 40, message = "La password deve essere tra 6 e 40 caratteri")
        String password,

        @NotBlank(message = "L'email è obbligatoria")
        @Size(max = 50, message = "L'email non può superare i 50 caratteri")
        @Email(message = "L'email deve essere valida")
        String email,

        @NotBlank(message = "Il nome è obbligatorio")
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        String cognome
) {}