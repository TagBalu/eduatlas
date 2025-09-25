package vacislavbaluyev.eduatlas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTTools jwtTools;
    private final UtenteService utenteService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedOperationException("Per favore inserisci il token nell'Authorization Header!");
        }

        try {
            String accessToken = authHeader.replace("Bearer ", "");
            jwtTools.verifyToken(accessToken);
            String username = jwtTools.extractSubject(accessToken); // Modifica qui per usare username invece di id

            // Recupera l'utente usando lo username
            UtenteDTO utente = utenteService.getUserByUsername(username);

            // Crea l'authority basata sul ruolo dell'utente
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + utente.ruolo().name())
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, // username come principal
                    null,    // credentials non necessarie qui
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Errore durante l'autenticazione: {}", e.getMessage());
            throw new UnauthorizedOperationException("Token non valido o scaduto");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                new AntPathMatcher().match("/auth/**", request.getServletPath()) ||
                (new AntPathMatcher().match("/paesi/**", request.getServletPath())
                        && "GET".equalsIgnoreCase(request.getMethod()));
    }
}