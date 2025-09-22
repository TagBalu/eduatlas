package vacislavbaluyev.eduatlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacislavbaluyev.eduatlas.entities.Utente;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}