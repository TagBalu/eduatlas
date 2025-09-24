package vacislavbaluyev.eduatlas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sistemaUniversitario")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SistemaUniversitario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "paeseId", nullable = false)
    private Paese paese;


    @Column(name = "durataBaseAnni")
    private Integer durataBaseAnni;

    @Column(name = "creditiPerAnno")
    private Integer creditiPerAnno;

    @Column(name = "livelloEQF")
    private String livelloEQF;
}
