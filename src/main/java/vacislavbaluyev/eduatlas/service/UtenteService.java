package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacislavbaluyev.eduatlas.entities.Utente;
import vacislavbaluyev.eduatlas.exception.ResourceNotFoundException;
import vacislavbaluyev.eduatlas.exception.UnauthorizedOperationException;
import vacislavbaluyev.eduatlas.exception.UserAlreadyExistsException;
import vacislavbaluyev.eduatlas.payload.*;
import vacislavbaluyev.eduatlas.repository.UtenteRepository;
import vacislavbaluyev.eduatlas.security.JwtTokenProvider;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public UtenteService(UtenteRepository utenteRepository,
                         PasswordEncoder passwordEncoder,
                         AuthenticationManager authenticationManager,
                         JwtTokenProvider tokenProvider) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public String authenticateUser(LoginDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.username(),
                        loginDto.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }

    public void registerUser(RegistrazioneDTO registrazioneDto) {
        if (utenteRepository.existsByUsername(registrazioneDto.username())) {
            throw new UserAlreadyExistsException("Username già in uso");
        }

        if (utenteRepository.existsByEmail(registrazioneDto.email())) {
            throw new UserAlreadyExistsException("Email già in uso");
        }

        Utente utente = Utente.builder()
                .username(registrazioneDto.username())
                .email(registrazioneDto.email())
                .password(passwordEncoder.encode(registrazioneDto.password()))
                .nome(registrazioneDto.nome())
                .cognome(registrazioneDto.cognome())
                .isAdmin(false)
                .isRootAdmin(false)
                .avatarUrl("https://ui-avatars.com/api/?name=" +
                        registrazioneDto.nome() + "+" + registrazioneDto.cognome())
                .build();

        utenteRepository.save(utente);
        log.info("Nuovo utente registrato: {}", utente.getUsername());
    }

    public void createAdmin(AdminCreationDTO adminDto, String requestingUsername) {
        Utente requestingUser = utenteRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (!requestingUser.isRootAdmin()) {
            throw new UnauthorizedOperationException("Solo l'admin root può creare altri admin");
        }

        if (utenteRepository.existsByUsername(adminDto.username())) {
            throw new UserAlreadyExistsException("Username già in uso");
        }

        if (utenteRepository.existsByEmail(adminDto.email())) {
            throw new UserAlreadyExistsException("Email già in uso");
        }

        Utente newAdmin = Utente.builder()
                .username(adminDto.username())
                .email(adminDto.email())
                .password(passwordEncoder.encode(adminDto.password()))
                .nome(adminDto.nome())
                .cognome(adminDto.cognome())
                .isAdmin(true)
                .isRootAdmin(false)
                .avatarUrl("https://ui-avatars.com/api/?name=" +
                        adminDto.nome() + "+" + adminDto.cognome())
                .build();

        utenteRepository.save(newAdmin);
        log.info("Nuovo admin creato da {}: {}", requestingUsername, newAdmin.getUsername());
    }

    public List<UtenteDTO> getAllUsers() {
        return utenteRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UtenteDTO getUserById(Long id) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));
        return convertToDTO(utente);
    }

    public UtenteDTO getUserByUsername(String username) {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username));
        return convertToDTO(utente);
    }

    public void updateUser(Long id, UtenteUpdateDTO utenteUpdateDto, String requestingUsername) {
        Utente requestingUser = utenteRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Utente targetUser = utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));

        // Verifica permessi
        if (!requestingUser.isAdmin() && !requestingUsername.equals(targetUser.getUsername())) {
            throw new UnauthorizedOperationException("Non hai i permessi per modificare questo utente");
        }

        // Un admin non-root non può modificare un root admin
        if (targetUser.isRootAdmin() && !requestingUser.isRootAdmin()) {
            throw new UnauthorizedOperationException("Non puoi modificare l'admin root");
        }

        if (utenteUpdateDto.email() != null &&
                !utenteUpdateDto.email().equals(targetUser.getEmail()) &&
                utenteRepository.existsByEmail(utenteUpdateDto.email())) {
            throw new UserAlreadyExistsException("Email già in uso");
        }

        updateUserFields(targetUser, utenteUpdateDto);
        utenteRepository.save(targetUser);
        log.info("Utente aggiornato da {}: {}", requestingUsername, targetUser.getUsername());
    }

    private void updateUserFields(Utente utente, UtenteUpdateDTO updateDto) {
        if (updateDto.nome() != null) {
            utente.setNome(updateDto.nome());
        }
        if (updateDto.cognome() != null) {
            utente.setCognome(updateDto.cognome());
        }
        if (updateDto.email() != null) {
            utente.setEmail(updateDto.email());
        }
        if (updateDto.password() != null) {
            utente.setPassword(passwordEncoder.encode(updateDto.password()));
        }

        // Aggiorna l'avatar URL se nome o cognome sono cambiati
        if (updateDto.nome() != null || updateDto.cognome() != null) {
            utente.setAvatarUrl("https://ui-avatars.com/api/?name=" +
                    utente.getNome() + "+" + utente.getCognome());
        }
    }

    private UtenteDTO convertToDTO(Utente utente) {
        return new UtenteDTO(
                utente.getId(),
                utente.getUsername(),
                utente.getEmail(),
                utente.getNome(),
                utente.getCognome(),
                utente.isAdmin(),
                utente.isRootAdmin(),
                utente.getAvatarUrl()
        );
    }
}