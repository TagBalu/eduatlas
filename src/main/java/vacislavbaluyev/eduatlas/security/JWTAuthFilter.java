package vacislavbaluyev.eduatlas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import vacislavbaluyev.eduatlas.exception.UnauthorizedOperationException;
import vacislavbaluyev.eduatlas.payload.UtenteDTO;
import vacislavbaluyev.eduatlas.service.UtenteService;
import vacislavbaluyev.eduatlas.tools.JWTTools;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JWTAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTTools jwtTools;

    @Autowired
    private UtenteService utenteService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Se il percorso è pubblico o non richiede autenticazione, procedi senza token
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verifica se l'header Authorization è presente e nel formato corretto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedOperationException("Per favore inserisci il token nell'Authorization Header nel formato corretto!");
        }

        try {
            String accessToken = authHeader.replace("Bearer ", "");

            // Verifica il token
            jwtTools.verifyToken(accessToken);

            // Estrae lo username dal token
            String username = jwtTools.extractSubject(accessToken);

            // Recupera l'utente dal database
            UtenteDTO currentUser = utenteService.getUserByUsername(username);

            // Crea l'oggetto Authentication con i ruoli appropriati
            List<SimpleGrantedAuthority> authorities;
            if (currentUser.isAdmin()) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("Errore durante l'autenticazione: {}", e.getMessage());
            throw new UnauthorizedOperationException("Token non valido o scaduto");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Gestione delle richieste OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        // Lista dei percorsi pubblici
        return pathMatcher.match("/api/auth/**", path) ||
                (pathMatcher.match("/api/paesi/**", path) && "GET".equalsIgnoreCase(request.getMethod()));
    }
}