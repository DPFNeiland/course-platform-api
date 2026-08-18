package ananditos.sandraxandreia.security;

import ananditos.sandraxandreia.service.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String TOKEN_EXPIRATION_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".expiration";
    private final JwtService jwtService;
    private final AuthorizationService authorizationService;
    private final JwtCookieService jwtCookieService;

    public JwtAuthenticationFilter(JwtService jwtService, AuthorizationService authorizationService,
                                   JwtCookieService jwtCookieService) {
        this.jwtService = jwtService;
        this.authorizationService = authorizationService;
        this.jwtCookieService = jwtCookieService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityRequestPolicy.isAuthenticationBootstrap(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtCookieService.obterToken(request).orElseGet(() -> obterBearer(request));
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtService.TokenValidado validado = jwtService.validar(token);
            UserDetails usuario = authorizationService.loadUserByUsername(validado.subject());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(TOKEN_EXPIRATION_ATTRIBUTE, validado.expiraEm());
        } catch (JwtInvalidoException ex) {
            SecurityContextHolder.clearContext();
            escreverNaoAutorizado(response, ex.getCodigo());
            return;
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            escreverNaoAutorizado(response, "TOKEN_INVALID");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String obterBearer(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : null;
    }

    private void escreverNaoAutorizado(HttpServletResponse response, String codigo) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":401,\"codigo\":\"" + codigo
                + "\",\"erro\":\"Token invalido ou expirado\"}");
    }
}
