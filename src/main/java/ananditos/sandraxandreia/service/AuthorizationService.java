package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static ananditos.sandraxandreia.service.validation.emailNormalizado.normalizarEMAIL;

@Service
public class AuthorizationService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public AuthorizationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailValor(normalizarEMAIL(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado para o e-mail: " + username));
    }
}
