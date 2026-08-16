package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.dto.request.LoginRequestDTO;
import ananditos.sandraxandreia.dto.response.LoginResponseDTO;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import ananditos.sandraxandreia.security.JwtService;
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

@RestController
@Tag(name = "Autenticacao", description = "Endpoint de login para o frontend")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Autentica um usuario por e-mail e senha")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailValor(normalizarEmail(request.getEmail()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha().getValor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos");
        }

        String perfil = toPerfil(usuario);
        String cargo = usuario.getPerfil() == null ? perfil.toUpperCase() : usuario.getPerfil().name();
        JwtService.TokenEmitido token = jwtService.emitir(usuario.getEmail().getValor());
        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail().getValor(),
                cargo,
                perfil,
                token.token(),
                token.expiraEm()
        );
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String toPerfil(Usuario usuario) {
        if (usuario.getPerfil() != null) {
            return usuario.getPerfil().name().toLowerCase();
        }
        if (usuario instanceof Aluno) {
            return "aluno";
        }
        if (usuario instanceof Professor) {
            return "professor";
        }
        return "curador";
    }
}
