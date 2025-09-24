package vacislavbaluyev.eduatlas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vacislavbaluyev.eduatlas.payload.AdminCreationDTO;
import vacislavbaluyev.eduatlas.payload.LoginDTO;
import vacislavbaluyev.eduatlas.payload.LoginResponseDTO;
import vacislavbaluyev.eduatlas.service.AuthService;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(response);
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