package vacislavbaluyev.eduatlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaValutazione;

import java.util.Optional;

public interface SistemaValutazioneRepository extends JpaRepository<SistemaValutazione, Long> {
    Optional<SistemaValutazione> findByPaese(Paese paese);
}