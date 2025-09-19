package vacislavbaluyev.eduatlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;

import java.util.List;
import java.util.Optional;

@Repository
public interface SistemaUniversitarioRepository extends JpaRepository<SistemaUniversitario, Long> {

    Optional<SistemaUniversitario> findByPaeseId(Long paeseId);

    @Query("SELECT s FROM SistemaUniversitario s WHERE s.durataBaseAnni = :durataAnni")
    List<SistemaUniversitario> findByDurataBaseAnni(@Param("durataAnni") Integer durataAnni);

    @Query("SELECT s FROM SistemaUniversitario s WHERE s.creditiPerAnno = :crediti")
    List<SistemaUniversitario> findByCreditiPerAnno(@Param("crediti") Integer crediti);

    @Query("SELECT s FROM SistemaUniversitario s WHERE s.livelloEQF = :livello")
    List<SistemaUniversitario> findByLivelloEQF(@Param("livello") String livello);
}