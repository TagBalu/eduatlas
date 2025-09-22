package vacislavbaluyev.eduatlas.payload;

import vacislavbaluyev.eduatlas.entities.Ruolo;
import java.util.Set;

public record UtenteDTO(
        Long id,
        String username,
        String email,
        String nome,
        String cognome,
        Set<Ruolo> ruoli,
        boolean enabled
) {}