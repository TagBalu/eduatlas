package vacislavbaluyev.eduatlas.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paese")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Paese {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    protected String nome;

    @Column(name = "anniScuolaObbligatoria")
    protected Integer anniSculaObbligaroia;
}
