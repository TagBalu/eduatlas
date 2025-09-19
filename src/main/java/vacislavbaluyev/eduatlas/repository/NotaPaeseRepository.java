package vacislavbaluyev.eduatlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vacislavbaluyev.eduatlas.entities.NotaPaese;

import java.util.List;

@Repository
public interface NotaPaeseRepository extends JpaRepository<NotaPaese, Long> {
    // Trova tutte le note per un dato paese
    List<NotaPaese> findByPaeseId(Long paeseId);

    // Trova tutte le note per una specifica colonna
    List<NotaPaese> findByColonnaRifermineto(int colonnaRifermineto);
}