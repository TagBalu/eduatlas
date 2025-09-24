package vacislavbaluyev.eduatlas.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vacislavbaluyev.eduatlas.payload.DettaglioPaeseDTO;
import vacislavbaluyev.eduatlas.payload.PaeseCreateDTO;
import vacislavbaluyev.eduatlas.service.PaeseService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/paesi")
@RequiredArgsConstructor
public class PaeseController {
    private final PaeseService paeseService;

    @GetMapping
    public ResponseEntity<List<DettaglioPaeseDTO>> getAllPaesi() {
        return ResponseEntity.ok(paeseService.getAllPaesi());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DettaglioPaeseDTO> getPaeseById(@PathVariable Long id) {
        return ResponseEntity.ok(paeseService.getPaeseById(id));
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<DettaglioPaeseDTO> getPaeseByNome(@PathVariable String nome) {
        return ResponseEntity.ok(paeseService.getPaeseByNome(nome));
    }

    @GetMapping("/anni-scuola/{anni}")
    public ResponseEntity<List<DettaglioPaeseDTO>> getPaesiByAnniScuola(@PathVariable Integer anni) {
        return ResponseEntity.ok(paeseService.getPaesiByAnniScuolaObbligatoria(anni));
    }

    @GetMapping("/durata-base/{anni}")
    public ResponseEntity<List<DettaglioPaeseDTO>> getPaesiByDurataBase(@PathVariable Integer anni) {
        return ResponseEntity.ok(paeseService.getPaesiByDurataBaseAnni(anni));
    }

    @GetMapping("/crediti-anno/{crediti}")
    public ResponseEntity<List<DettaglioPaeseDTO>> getPaesiByCreditiAnno(@PathVariable Integer crediti) {
        return ResponseEntity.ok(paeseService.getPaesiByCreditiPerAnno(crediti));
    }

    @GetMapping("/livello-eqf/{livello}")
    public ResponseEntity<List<DettaglioPaeseDTO>> getPaesiByLivelloEQF(@PathVariable String livello) {
        return ResponseEntity.ok(paeseService.getPaesiByLivelloEQF(livello));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT_ADMIN')")
    public ResponseEntity<DettaglioPaeseDTO> createPaese(@Valid @RequestBody PaeseCreateDTO createDTO) {
        return ResponseEntity.ok(paeseService.createPaese(createDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT_ADMIN')")
    public ResponseEntity<Void> deletePaese(@PathVariable Long id) {
        paeseService.deletePaese(id);
        return ResponseEntity.ok().build();
    }
}