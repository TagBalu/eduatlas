package vacislavbaluyev.eduatlas.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UtenteUpdateDTO(
        @Size(max = 50)
        @Email(message = "L'email deve essere valida")
        String email,

        @Size(min = 6, max = 40, message = "La password deve essere tra 6 e 40 caratteri")
        String password,

        String nome,

        String cognome
) {}