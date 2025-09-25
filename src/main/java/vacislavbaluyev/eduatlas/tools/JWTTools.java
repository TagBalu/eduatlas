package vacislavbaluyev.eduatlas.tools;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vacislavbaluyev.eduatlas.entities.Utente;
import vacislavbaluyev.eduatlas.exception.UnauthorizedOperationException;

import java.util.Date;

@Component
@Slf4j
public class JWTTools {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Utente utente) {
        return Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 giorni
                .subject(utente.getUsername())
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public void verifyToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token);
        } catch (Exception ex) {
            log.error("Errore nella verifica del token: {}", ex.getMessage());
            throw new UnauthorizedOperationException("Token non valido o scaduto");
        }
    }

    public String extractSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception ex) {
            log.error("Errore nell'estrazione del subject: {}", ex.getMessage());
            throw new UnauthorizedOperationException("Errore nell'elaborazione del token");
        }
    }
}