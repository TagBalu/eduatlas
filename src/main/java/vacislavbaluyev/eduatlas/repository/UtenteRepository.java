package vacislavbaluyev.eduatlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vacislavbaluyev.eduatlas.entities.Utente;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
    Optional<Utente> findByEmail(String email);
    List<Utente>findByIsRootAdmin(boolean isRootAdmin);
}
