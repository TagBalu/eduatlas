package vacislavbaluyev.eduatlas.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sistemaValutazione")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SistemaValutazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paeseId", nullable = false)
    private Paese paese;

    @Column(name = "votoA")
    private String votoA;

    @Column(name = "votoB")
    private String votoB;

    @Column(name = "votoC")
    private String votoC;

    @Column(name = "votoDE")
    private String votoDE;

    @Column(name = "votoF")
    private String votoF;

    @Enumerated(EnumType.STRING)
    @Column(name = "scalaTipo")
    private TipoScala scalaTipo;


}
