package vacislavbaluyev.eduatlas.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "paese")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paese {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String nome;

    @Column(name = "anniSculaObbligaroia")
    private Integer anniSculaObbligaroia;
}
