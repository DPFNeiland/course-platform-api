package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;
import ananditos.sandraxandreia.dto.request.LoginRequestDTO;
import ananditos.sandraxandreia.dto.response.LoginResponseDTO;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static ananditos.sandraxandreia.service.validation.emailNormalizado.normalizarEMAIL;

@RestController
@Tag(name = "Autenticacao", description = "Endpoint de login para o frontend")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Autentica um usuario por e-mail e senha")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailValor(normalizarEMAIL(request.getEmail()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos");
        }

        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail().getValor(),
                usuario.getCargo(),
                toPerfil(usuario.getCargo())
        );
    }

    private String toPerfil(UsuarioCargo cargo) {
        if (cargo == null) {
            return "anonimo";
        }
        return cargo.name().toLowerCase();
    }
}
