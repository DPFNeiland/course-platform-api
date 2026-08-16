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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        JwtService shortLived = new JwtService("test-only-jwt-secret-with-at-least-32-characters", 1);
        String token = shortLived.emitir("aluno@teste.com").token();
        Thread.sleep(1100);

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
    void loginDeveEmitirJwtParaAlunoProfessorECurador() throws Exception {
        validarLogin("aluno@teste.com", "aluno");
        validarLogin("professor@teste.com", "professor");
        validarLogin("curador@teste.com", "curador");
    }

    @Test
    void loginDeveGravarJwtSomenteEmCookieHttpOnly() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aluno@teste.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=Strict")));
    }

    @Test
    void logoutDeveRemoverCookieDeSessao() throws Exception {
        mockMvc.perform(post("/logout").cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")));
    }

    private void validarLogin(String email, String perfil) throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value(perfil))
                .andExpect(jsonPath("$.cargo").value(perfil.toUpperCase()))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.expiraEm").isNotEmpty());
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
                .getCookie("SXA_SESSION");
        org.assertj.core.api.Assertions.assertThat(cookie).isNotNull();
        return cookie;
    }

    private void validarLoginInvalido(String email, String senha) throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
