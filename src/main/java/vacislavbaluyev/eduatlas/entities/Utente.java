package vacislavbaluyev.eduatlas.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utente")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nome;

    private String avatarUrl;


    @Column(nullable = false)
    private Boolean isRootAdmin= false; // flag per l'admin principale che può creare altri admin

    @Column(nullable = false)
    private Boolean isAdmin= true; // tutti gli utenti saranno admin
}
