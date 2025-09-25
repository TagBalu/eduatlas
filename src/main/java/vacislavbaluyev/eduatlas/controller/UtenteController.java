package vacislavbaluyev.eduatlas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vacislavbaluyev.eduatlas.payload.AdminCreationDTO;
import vacislavbaluyev.eduatlas.payload.UtenteDTO;
import vacislavbaluyev.eduatlas.payload.UtenteUpdateDTO;
import vacislavbaluyev.eduatlas.service.AuthService;
import vacislavbaluyev.eduatlas.service.UtenteService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/utenti")
@RequiredArgsConstructor
public class UtenteController {
    private final UtenteService utenteService;

    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROOT_ADMIN')")
    public ResponseEntity<List<UtenteDTO>> getAllUsers() {
        return ResponseEntity.ok(utenteService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROOT_ADMIN')")
    public ResponseEntity<UtenteDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(utenteService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ROOT_ADMIN')")
    public ResponseEntity<UtenteDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(utenteService.getUserByUsername(username));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROOT_ADMIN')")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @RequestBody UtenteUpdateDTO updateDTO,
            Principal principal) {
        utenteService.updateUser(id, updateDTO, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ResponseEntity<String> createAdmin(
            @Valid @RequestBody AdminCreationDTO adminDTO,
            Principal principal) {
        authService.createAdmin(adminDTO, principal.getName());
        return ResponseEntity.ok("Admin creato con successo");
    }
}