package vacislavbaluyev.eduatlas.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "titoloStudio")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitoloStudio {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
    @JoinColumn(name = "paeseId", nullable = false)
    private Paese paese;

   @Column(name = "denominazione", nullable = false)
    private String denominazione;

}
