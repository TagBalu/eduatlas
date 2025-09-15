package vacislavbaluyev.eduatlas.entities;


import jakarta.persistence.*;

@Entity
@Table(name = "notaPaese")

public class NotaPaese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paeseId", nullable = false)
    private Paese paese;

    @Column(name = "nota", nullable = false)
    private String nota;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoNota")
    private TipoNotaEnum topoNota;
}
