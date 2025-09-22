package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vacislavbaluyev.eduatlas.entities.Ruolo;
import vacislavbaluyev.eduatlas.entities.Utente;
import vacislavbaluyev.eduatlas.exception.ResourceNotFoundException;
import vacislavbaluyev.eduatlas.exception.UnauthorizedOperationException;
import vacislavbaluyev.eduatlas.exception.UserAlreadyExistsException;
import vacislavbaluyev.eduatlas.payload.AdminCreationDTO;
import vacislavbaluyev.eduatlas.payload.LoginDTO;
import vacislavbaluyev.eduatlas.payload.RegistrazioneDTO;
import vacislavbaluyev.eduatlas.repository.UtenteRepository;
import vacislavbaluyev.eduatlas.tools.JWTTools;

@Service
@Slf4j
public class AuthService {
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTTools jwtTools;

    public AuthService(UtenteRepository utenteRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JWTTools jwtTools) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTools = jwtTools;
    }

    public String authenticateUser(LoginDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password())
        );
        return jwtTools.generateToken(authentication);
    }

    public void registerUser(RegistrazioneDTO registrazioneDto) {
        checkUserExists(registrazioneDto.username(), registrazioneDto.email());

        Utente utente = buildUser(registrazioneDto, Ruolo.ADMIN);
        utenteRepository.save(utente);
        log.info("Nuovo utente registrato: {}", utente.getUsername());
    }

    public void createAdmin(AdminCreationDTO adminDto, String requestingUsername) {
        Utente requestingUser = utenteRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (requestingUser.getRuolo() != Ruolo.ROOT_ADMIN) {
            throw new UnauthorizedOperationException("Solo l'utente root può creare admin");
        }

        checkUserExists(adminDto.username(), adminDto.email());

        Utente admin = buildUser(adminDto, Ruolo.ADMIN);
        utenteRepository.save(admin);
        log.info("Nuovo admin creato da {}: {}", requestingUsername, admin.getUsername());
    }

    private void checkUserExists(String username, String email) {
        if (utenteRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username già in uso");
        }
        if (utenteRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email già in uso");
        }
    }

    private Utente buildUser(RegistrazioneDTO dto, Ruolo ruolo) {
        return Utente.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .nome(dto.nome())
                .cognome(dto.cognome())
                .ruolo(ruolo)
                .avatarUrl(generateAvatarUrl(dto.nome(), dto.cognome()))
                .build();
    }

    private String generateAvatarUrl(String nome, String cognome) {
        return "https://ui-avatars.com/api/?name=" + nome + "+" + cognome;
    }
}