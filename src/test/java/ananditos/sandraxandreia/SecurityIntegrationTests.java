package ananditos.sandraxandreia;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.aluno.StatusAluno;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.TipoEnsinoProfessor;
import ananditos.sandraxandreia.domain.curador.Curador;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.repository.AlunoRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import ananditos.sandraxandreia.repository.CuradorRepository;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import ananditos.sandraxandreia.security.JwtService;
import ananditos.sandraxandreia.security.JwtCookieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@WebAppConfiguration
class SecurityIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CuradorRepository curadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        usuarioRepository.deleteAll();

        alunoRepository.save(new Aluno(
                null,
                "Aluno Teste",
                "aluno@teste.com",
                passwordEncoder.encode("123456"),
                "52998224725",
                GeneroUsuario.MASCULINO,
                "1/1/2000",
                "AB1234",
                StatusAluno.CURSANDO
        ));

        professorRepository.save(new Professor(
                null,
                "Professor Teste",
                "professor@teste.com",
                passwordEncoder.encode("123456"),
                "39053344705",
                GeneroUsuario.FEMININO,
                "1/1/1985",
                "Engenharia de Software",
                100.0,
                TipoEnsinoProfessor.AMBOS
        ));

        curadorRepository.save(new Curador(
                null,
                "Curador Teste",
                "curador@teste.com",
                passwordEncoder.encode("123456"),
                "11144477735",
                GeneroUsuario.NAO_INFORMADO,
                "1/1/1980"
        ));
    }

    @Test
    void devePermitirAlunoNoEndpointDeAluno() throws Exception {
        mockMvc.perform(get("/aluno").header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAlunoNoEndpointDeCurso() throws Exception {
        mockMvc.perform(get("/curso").header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirProfessorNoEndpointDeCurso() throws Exception {
        mockMvc.perform(get("/curso").header("Authorization", bearer("professor@teste.com")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearProfessorNoEndpointDeMatricula() throws Exception {
        mockMvc.perform(get("/matricula").header("Authorization", bearer("professor@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarTokenInvalido() throws Exception {
        mockMvc.perform(get("/aluno").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALID"));
    }

    @Test
    void deveIdentificarTokenExpirado() throws Exception {
        JwtService shortLived = new JwtService(
                "test-only-jwt-secret-with-at-least-32-characters",
                1,
                Clock.fixed(Instant.now().minusSeconds(2), ZoneOffset.UTC));
        String token = shortLived.emitir("aluno@teste.com").token();

        mockMvc.perform(get("/aluno").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_EXPIRED"));
    }

    @Test
    void deveRejeitarBasicAuth() throws Exception {
        mockMvc.perform(get("/aluno").header("Authorization", "Basic YWx1bm9AdGVzdGUuY29tOjEyMzQ1Ng=="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/aluno"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarTokenAssinadoComOutroSegredo() throws Exception {
        JwtService outroEmissor = new JwtService("another-test-secret-with-at-least-32-characters", 3600);
        String token = outroEmissor.emitir("aluno@teste.com").token();

        mockMvc.perform(get("/aluno").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALID"));
    }

    @Test
    void loginDeveRejeitarSenhaIncorretaEUsuarioInexistente() throws Exception {
        validarLoginInvalido("aluno@teste.com", "senha-incorreta");
        validarLoginInvalido("inexistente@teste.com", "123456");
    }

    @Test
    void cookieRetornadoNoLoginDeveAutorizarOsTresPerfis() throws Exception {
        mockMvc.perform(get("/aluno").cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/curso").cookie(cookieObtidoNoLogin("professor@teste.com")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/curador").cookie(cookieObtidoNoLogin("curador@teste.com")))
                .andExpect(status().isOk());
    }

    @Test
    void loginDeveCriarSessaoParaAlunoProfessorECurador() throws Exception {
        validarLogin("aluno@teste.com", "aluno");
        validarLogin("professor@teste.com", "professor");
        validarLogin("curador@teste.com", "curador");
    }

    @Test
    void loginDeveRetornarExpiracaoEJwtSomenteNoCookie() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aluno@teste.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.expiraEm").isNotEmpty())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("SameSite=Strict"))));
    }

    @Test
    void cookieInvalidoDevePrevalecerSobreBearerValido() throws Exception {
        mockMvc.perform(get("/aluno")
                        .cookie(new Cookie(JwtCookieService.COOKIE_NAME, "token.invalido.aqui"))
                        .header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALID"));
    }

    @Test
    void logoutDeveRemoverCookieDeSessao() throws Exception {
        Cookie sessionCookie = cookieObtidoNoLogin("aluno@teste.com");
        Cookie csrfCookie = cookieCsrf(sessionCookie);

        Cookie removedSession = mockMvc.perform(post("/logout")
                        .cookie(sessionCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse()
                .getCookie(JwtCookieService.COOKIE_NAME);

        org.assertj.core.api.Assertions.assertThat(removedSession).isNotNull();
        org.assertj.core.api.Assertions.assertThat(removedSession.getMaxAge()).isZero();
        org.assertj.core.api.Assertions.assertThat(removedSession.isHttpOnly()).isTrue();
    }

    @Test
    void operacaoComCookieSemCsrfDeveSerBloqueada() throws Exception {
        mockMvc.perform(post("/logout").cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointCsrfDeveEmitirCookieLegivelSemExporJwt() throws Exception {
        mockMvc.perform(get("/csrf").cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("HttpOnly"))));
    }

    @Test
    void corsDeveAceitarCredenciaisApenasDeOrigemPermitida() throws Exception {
        mockMvc.perform(options("/aluno")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));

        mockMvc.perform(options("/aluno")
                        .header(HttpHeaders.ORIGIN, "https://origem-nao-permitida.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private void validarLogin(String email, String perfil) throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value(perfil))
                .andExpect(jsonPath("$.cargo").value(perfil.toUpperCase()))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.expiraEm").isNotEmpty())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    private String bearer(String email) {
        return "Bearer " + jwtService.emitir(email).token();
    }

    private Cookie cookieObtidoNoLogin(String email) throws Exception {
        Cookie cookie = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(JwtCookieService.COOKIE_NAME);
        org.assertj.core.api.Assertions.assertThat(cookie).isNotNull();
        return cookie;
    }

    private Cookie cookieCsrf(Cookie sessionCookie) throws Exception {
        Cookie cookie = mockMvc.perform(get("/csrf").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("XSRF-TOKEN");
        org.assertj.core.api.Assertions.assertThat(cookie).isNotNull();
        org.assertj.core.api.Assertions.assertThat(cookie.isHttpOnly()).isFalse();
        return cookie;
    }

    private void validarLoginInvalido(String email, String senha) throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
