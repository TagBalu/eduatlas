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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedOperationException("Per favore inserisci il token nell'Authorization Header nel formato corretto!");
        }

        String accessToken = authHeader.replace("Bearer ", "");

        // Verifica il token
        jwtTools.verifyToken(accessToken); // Usa il metodo della tua classe JWTTools

        // Estrae lo username dal token
        String username = jwtTools.extractSubject(accessToken); // Usa il metodo della tua classe JWTTools

        // Recupera l'utente dal database
        UtenteDTO currentUser = utenteService.getUserByUsername(username);

        // Crea l'oggetto Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                currentUser.isAdmin() ?
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")) :
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                new AntPathMatcher().match("/api/auth/**", request.getServletPath());
    }
}