package vacislavbaluyev.eduatlas.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vacislavbaluyev.eduatlas.entities.Ruolo;
import vacislavbaluyev.eduatlas.entities.Utente;
import vacislavbaluyev.eduatlas.exception.ResourceNotFoundException;
import vacislavbaluyev.eduatlas.exception.UnauthorizedOperationException;
import vacislavbaluyev.eduatlas.exception.UserAlreadyExistsException;
import vacislavbaluyev.eduatlas.payload.*;
import vacislavbaluyev.eduatlas.repository.UtenteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UtenteService {
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UtenteDTO> getAllUsers() {
        return utenteRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UtenteDTO getUserById(Long id) {
        return convertToDTO(findUserById(id));
    }

    public UtenteDTO getUserByUsername(String username) {
        return convertToDTO(findUserByUsername(username));
    }


    private Utente findUserById(Long id) {
        return utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));
    }

    private Utente findUserByUsername(String username) {
        return utenteRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username));
    }

    private void checkUpdatePermissions(Utente requestingUser, Utente targetUser) {
        boolean isAdmin = requestingUser.getRuolo() == Ruolo.ADMIN;
        boolean isRootAdmin = requestingUser.getRuolo() == Ruolo.ROOT_ADMIN;
        boolean isSameUser = requestingUser.getUsername().equals(targetUser.getUsername());

        if (!isAdmin && !isRootAdmin && !isSameUser) {
            throw new UnauthorizedOperationException("Non hai i permessi per modificare questo utente");
        }

        if (targetUser.getRuolo() == Ruolo.ROOT_ADMIN && !isRootAdmin) {
            throw new UnauthorizedOperationException("Non puoi modificare l'admin root");
        }
    }

    private void checkEmailAvailability(UtenteUpdateDTO updateDto, Utente targetUser) {
        if (updateDto.email() != null &&
                !updateDto.email().equals(targetUser.getEmail()) &&
                utenteRepository.existsByEmail(updateDto.email())) {
            throw new UserAlreadyExistsException("Email già in uso");
        }
    }

    public void updateUser(Long id, UtenteUpdateDTO utenteUpdateDto, String requestingUsername) {
        Utente requestingUser = utenteRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Utente targetUser = utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));

        // Verifica permessi
        if (requestingUser.getRuolo() != Ruolo.ADMIN &&
            requestingUser.getRuolo() != Ruolo.ROOT_ADMIN &&
            !requestingUsername.equals(targetUser.getUsername())) {
            throw new UnauthorizedOperationException("Non hai i permessi per modificare questo utente");
        }

        // Un admin non può modificare un root admin
        if (targetUser.getRuolo() == Ruolo.ROOT_ADMIN &&
            requestingUser.getRuolo() != Ruolo.ROOT_ADMIN) {
            throw new UnauthorizedOperationException("Non puoi modificare l'admin root");
        }

        // Un admin non-root non può modificare un root admin

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
                utente.getAvatarUrl(),
                utente.getRuolo()
        );
    }

    public void deleteAdmin(Long id, String requestingUsername) {
        // Trova l'utente che fa la richiesta
        Utente requestingUser = utenteRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utente richiedente non trovato"));

        // Verifica che l'utente richiedente sia ROOT_ADMIN
        if (requestingUser.getRuolo() != Ruolo.ROOT_ADMIN) {
            throw new UnauthorizedOperationException("Solo i ROOT_ADMIN possono eliminare gli admin");
        }

        // Trova l'utente da eliminare
        Utente targetUser = utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin non trovato con id: " + id));

        // Verifica che l'utente da eliminare sia un ADMIN (non ROOT_ADMIN)
        if (targetUser.getRuolo() != Ruolo.ADMIN) {
            if (targetUser.getRuolo() == Ruolo.ROOT_ADMIN) {
                throw new UnauthorizedOperationException("Non è possibile eliminare un ROOT_ADMIN");
            } else {
                throw new UnauthorizedOperationException("L'utente selezionato non è un admin");
            }
        }

        // Verifica che l'admin non stia cercando di eliminare se stesso
        if (targetUser.getUsername().equals(requestingUsername)) {
            throw new UnauthorizedOperationException("Non puoi eliminare te stesso");
        }

        // Procedi con l'eliminazione
        utenteRepository.delete(targetUser);
        log.info("Admin {} eliminato da {}", targetUser.getUsername(), requestingUsername);
    }
}