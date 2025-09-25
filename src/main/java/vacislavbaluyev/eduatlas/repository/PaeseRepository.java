package vacislavbaluyev.eduatlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vacislavbaluyev.eduatlas.entities.Paese;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaeseRepository extends JpaRepository<Paese, Long> {
    Optional<Paese> findByNome(String nome);

    boolean existsByNome(String nome);

    @Query("SELECT p FROM Paese p WHERE p.anniSculaObbligaroia = :anni")
    List<Paese> findByAnniScuolaObbligatoria(@Param("anni") Integer anni);

    List<Paese> findByNomeContainingIgnoreCase(String nome);
}
