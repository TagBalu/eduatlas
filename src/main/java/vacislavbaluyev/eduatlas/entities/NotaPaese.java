package vacislavbaluyev.eduatlas.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notaPaese")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

public class NotaPaese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paeseId", nullable = false)
    private Paese paese;

    @Column(name = "nota", nullable = false)
    private String nota;


    @Column(name = "colonnaRifermineto")
    private int colonnaRifermineto;
}
