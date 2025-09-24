package vacislavbaluyev.eduatlas.payload;

import vacislavbaluyev.eduatlas.entities.Ruolo;


public record UtenteDTO(
        Long id,
        String username,
        String email,
        String nome,
        String cognome,
        String avatarUrl,
        Ruolo ruolo
) {public boolean isAdmin() {
    return ruolo == Ruolo.ADMIN;
}
}
