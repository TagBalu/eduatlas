package vacislavbaluyev.eduatlas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sistemaValutazione")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SistemaValutazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paeseId", nullable = false)
    private Paese paese;

    @Column(name = "votoMassimo")
    private String votoMassimo;

    @Column(name = "votoMinimo")
    private String votoMinimo;

    @Column(name = "votoSufficienza")
    private String votoSufficienza;

    @Enumerated(EnumType.STRING)
    @Column(name = "scalaTipo")
    private ScalaTipoEnum scalaTipo;

}
