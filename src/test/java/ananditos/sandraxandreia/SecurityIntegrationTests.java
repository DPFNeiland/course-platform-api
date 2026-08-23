package ananditos.sandraxandreia;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.aluno.StatusAluno;
import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.TipoEnsinoProfessor;
import ananditos.sandraxandreia.domain.curador.Curador;
import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.repository.AlunoRepository;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.MatriculaRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SecurityIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

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

        matriculaRepository.deleteAll();
        cursoRepository.deleteAll();
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
        Long alunoId = alunoAutenticado().getId();
        mockMvc.perform(get("/aluno/{id}", alunoId).header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAlunoNoEndpointDeCurso() throws Exception {
        mockMvc.perform(get("/curso").header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirProfessorNoEndpointDeCurso() throws Exception {
        Long professorId = professorAutenticado().getId();
        mockMvc.perform(get("/curso/professor/{id}", professorId)
                        .header("Authorization", bearer("professor@teste.com")))
                .andExpect(status().isOk());
    }

    @Test
    void professorDeveCriarCursoProprioEMasNaoParaOutroProfessor() throws Exception {
        Professor professor = professorAutenticado();
        Professor outroProfessor = novoOutroProfessor();

        mockMvc.perform(post("/curso")
                        .header("Authorization", bearer("professor@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cursoPayload("Curso do professor autenticado", professor.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorId").value(professor.getId()));

        mockMvc.perform(post("/curso")
                        .header("Authorization", bearer("professor@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cursoPayload("Curso vinculado indevidamente", outroProfessor.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void curadorDeveListarCursosPendentesDeAprovacao() throws Exception {
        Curso pendente = novoCursoEmAvaliacao("Curso aguardando aprovacao", professorAutenticado());

        mockMvc.perform(get("/curso").header("Authorization", bearer("curador@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", org.hamcrest.Matchers.hasItem(pendente.getId().intValue())))
                .andExpect(jsonPath("$[*].status", org.hamcrest.Matchers.hasItem("EM_AVALIACAO")));
    }

    @Test
    void deveBloquearProfessorNoEndpointDeMatricula() throws Exception {
        mockMvc.perform(get("/matricula").header("Authorization", bearer("professor@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornarSomenteMatriculasDoAlunoAutenticado() throws Exception {
        Aluno alunoAutenticado = alunoRepository.findAll().stream()
                .filter(aluno -> aluno.getEmail().getValor().equals("aluno@teste.com"))
                .findFirst()
                .orElseThrow();
        Aluno outroAluno = alunoRepository.save(new Aluno(
                null,
                "Outro Aluno",
                "outro.aluno@teste.com",
                passwordEncoder.encode("123456"),
                "24681357928",
                GeneroUsuario.NAO_INFORMADO,
                "1/1/2001",
                "CD5678",
                StatusAluno.CURSANDO
        ));
        Professor professor = professorRepository.findAll().getFirst();
        Curso cursoDoAluno = novoCursoAprovado("Curso do aluno autenticado", professor);
        Curso cursoDoOutroAluno = novoCursoAprovado("Curso do outro aluno", professor);
        matriculaRepository.save(novaMatricula(alunoAutenticado, cursoDoAluno));
        Matricula matriculaDoOutroAluno = matriculaRepository.save(novaMatricula(outroAluno, cursoDoOutroAluno));

        mockMvc.perform(get("/matricula/me").header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].alunoId").value(alunoAutenticado.getId()))
                .andExpect(jsonPath("$[0].cursoId").value(cursoDoAluno.getId()));

        mockMvc.perform(get("/matricula/{id}", matriculaDoOutroAluno.getId())
                        .header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearAlunoNasConsultasDeMatriculasDeTerceiros() throws Exception {
        Long outroAlunoId = alunoRepository.save(new Aluno(
                null,
                "Outro Aluno",
                "outro.aluno@teste.com",
                passwordEncoder.encode("123456"),
                "24681357928",
                GeneroUsuario.NAO_INFORMADO,
                "1/1/2001",
                "CD5678",
                StatusAluno.CURSANDO
        )).getId();

        mockMvc.perform(get("/matricula").header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/matricula/aluno/{alunoId}", outroAlunoId)
                        .header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/matricula/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirCuradorNaConsultaGlobalDeMatriculas() throws Exception {
        mockMvc.perform(get("/matricula").header("Authorization", bearer("curador@teste.com")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearCriacaoDeMatriculaParaOutroAluno() throws Exception {
        Aluno outroAluno = novoOutroAluno();
        Curso curso = novoCursoAprovado("Curso para tentativa indevida", professorRepository.findAll().getFirst());

        mockMvc.perform(post("/matricula")
                        .header("Authorization", bearer("aluno@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaPayload(outroAluno.getId(), curso.getId(), "ATIVA")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Aluno nao pode alterar matriculas de outro aluno"));

        org.assertj.core.api.Assertions.assertThat(matriculaRepository.findAll()).isEmpty();
    }

    @Test
    void devePermitirAlunoAlterarEExcluirAPropriaMatricula() throws Exception {
        Aluno aluno = alunoAutenticado();
        Curso curso = novoCursoAprovado("Curso da propria matricula", professorRepository.findAll().getFirst());
        Matricula matricula = matriculaRepository.save(novaMatricula(aluno, curso));

        mockMvc.perform(put("/matricula/{id}", matricula.getId())
                        .header("Authorization", bearer("aluno@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaPayload(aluno.getId(), curso.getId(), "ENCERRADA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));

        mockMvc.perform(patch("/matricula/{id}/status", matricula.getId())
                        .header("Authorization", bearer("aluno@teste.com"))
                        .param("novoStatus", "ATIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVA"));

        mockMvc.perform(delete("/matricula/{id}", matricula.getId())
                        .header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(matriculaRepository.findById(matricula.getId())).isEmpty();
    }

    @Test
    void deveBloquearAlteracaoStatusEExclusaoDeMatriculaDeTerceiro() throws Exception {
        Aluno alunoAutenticado = alunoAutenticado();
        Aluno outroAluno = novoOutroAluno();
        Curso curso = novoCursoAprovado("Curso da matricula de terceiro", professorRepository.findAll().getFirst());
        Matricula matricula = matriculaRepository.save(novaMatricula(outroAluno, curso));

        mockMvc.perform(put("/matricula/{id}", matricula.getId())
                        .header("Authorization", bearer("aluno@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaPayload(alunoAutenticado.getId(), curso.getId(), "ENCERRADA")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/matricula/{id}/status", matricula.getId())
                        .header("Authorization", bearer("aluno@teste.com"))
                        .param("novoStatus", "ENCERRADA"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/matricula/{id}", matricula.getId())
                        .header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(matriculaRepository.findById(matricula.getId()))
                .get()
                .extracting(Matricula::getStatus)
                .isEqualTo(StatusMatricula.ATIVA);
    }

    @Test
    void devePermitirCuradorAlterarStatusDeQualquerMatricula() throws Exception {
        Aluno aluno = alunoAutenticado();
        Curso curso = novoCursoAprovado("Curso administrado pelo curador", professorRepository.findAll().getFirst());
        Matricula matricula = matriculaRepository.save(novaMatricula(aluno, curso));

        mockMvc.perform(patch("/matricula/{id}/status", matricula.getId())
                        .header("Authorization", bearer("curador@teste.com"))
                        .param("novoStatus", "ENCERRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
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
        mockMvc.perform(get("/aluno/{id}", alunoAutenticado().getId())
                        .cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/curso/professor/{id}", professorAutenticado().getId())
                        .cookie(cookieObtidoNoLogin("professor@teste.com")))
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
        mockMvc.perform(loginRequest("aluno@teste.com", "123456"))
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
    void cookieComBearerInvalidoSemCsrfDeveSerBloqueado() throws Exception {
        mockMvc.perform(post("/logout")
                        .cookie(cookieObtidoNoLogin("aluno@teste.com"))
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CSRF_INVALID"));
    }

    @Test
    void cookieComBearerValidoSemCsrfDeveSerBloqueado() throws Exception {
        mockMvc.perform(post("/logout")
                        .cookie(cookieObtidoNoLogin("aluno@teste.com"))
                        .header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CSRF_INVALID"));
    }

    @Test
    void cookieComBearerECsrfValidosDeveExecutarNormalmente() throws Exception {
        Cookie sessionCookie = cookieObtidoNoLogin("aluno@teste.com");
        Cookie csrfCookie = cookieCsrf(sessionCookie);

        mockMvc.perform(post("/logout")
                        .cookie(sessionCookie, csrfCookie)
                        .header("Authorization", bearer("aluno@teste.com"))
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent());
    }

    @Test
    void bearerValidoSemCookieDeveContinuarDispensadoDeCsrf() throws Exception {
        mockMvc.perform(post("/logout").header("Authorization", bearer("aluno@teste.com")))
                .andExpect(status().isNoContent());
    }

    @Test
    void rotaPublicaComCookieSemCsrfDeveSerBloqueada() throws Exception {
        mockMvc.perform(post("/login")
                        .cookie(cookieObtidoNoLogin("aluno@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aluno@teste.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CSRF_INVALID"));
    }

    @Test
    void loginComCookieValidoDeveTrocarSessaoQuandoCsrfForValido() throws Exception {
        mockMvc.perform(loginRequest("professor@teste.com", "123456",
                        cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("professor"))
                .andExpect(cookie().exists(JwtCookieService.COOKIE_NAME));
    }

    @Test
    void loginComCookieInvalidoVazioOuExpiradoNaoDeveFicarBloqueado() throws Exception {
        JwtService expiredIssuer = new JwtService(
                "test-only-jwt-secret-with-at-least-32-characters",
                1,
                Clock.fixed(Instant.now().minusSeconds(2), ZoneOffset.UTC));

        for (Cookie previousCookie : java.util.List.of(
                new Cookie(JwtCookieService.COOKIE_NAME, "token.invalido.aqui"),
                new Cookie(JwtCookieService.COOKIE_NAME, ""),
                new Cookie(JwtCookieService.COOKIE_NAME, expiredIssuer.emitir("aluno@teste.com").token()))) {
            mockMvc.perform(loginRequest("aluno@teste.com", "123456", previousCookie))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists(JwtCookieService.COOKIE_NAME));
        }
    }

    @Test
    void cadastroComCookieAnteriorDeveAceitarCsrfValido() throws Exception {
        Cookie previousSession = cookieObtidoNoLogin("aluno@teste.com");
        Cookie csrfCookie = cookieCsrf(previousSession);

        mockMvc.perform(post("/aluno")
                        .cookie(previousSession, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome":"Novo Aluno",
                                  "email":"novo.aluno@teste.com",
                                  "cpf":"24681357928",
                                  "senha":"123456",
                                  "genero":"NAO_INFORMADO",
                                  "dataNascimento":"1/1/2000",
                                  "ra":"CD5678",
                                  "status":"A_CURSAR"
                                }
                                """))
                .andExpect(status().isCreated());
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
                .andExpect(jsonPath("$.headerName").doesNotExist())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("HttpOnly"))));
    }

    @Test
    void endpointCsrfDeveEstarDisponivelAntesDoLogin() throws Exception {
        mockMvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("XSRF-TOKEN")));
    }

    @Test
    void csrfAusenteDeveRetornarCodigoIdentificavel() throws Exception {
        mockMvc.perform(post("/logout").cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CSRF_INVALID"));
    }

    @Test
    void sessaoDeveSerRecuperadaPeloCookieSemExporJwt() throws Exception {
        mockMvc.perform(get("/session").cookie(cookieObtidoNoLogin("aluno@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("aluno"))
                .andExpect(jsonPath("$.email").value("aluno@teste.com"))
                .andExpect(jsonPath("$.expiraEm").isNotEmpty())
                .andExpect(jsonPath("$.token").doesNotExist());

        mockMvc.perform(get("/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sessaoDeveRejeitarCookiesInvalidosEExpiradosComCodigoAdequado() throws Exception {
        JwtService expiredIssuer = new JwtService(
                "test-only-jwt-secret-with-at-least-32-characters",
                1,
                Clock.fixed(Instant.now().minusSeconds(2), ZoneOffset.UTC));

        mockMvc.perform(get("/session")
                        .cookie(new Cookie(JwtCookieService.COOKIE_NAME, "token.invalido.aqui")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALID"));

        mockMvc.perform(get("/session")
                        .cookie(new Cookie(JwtCookieService.COOKIE_NAME,
                                expiredIssuer.emitir("aluno@teste.com").token())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_EXPIRED"));
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
        mockMvc.perform(loginRequest(email, "123456"))
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
        Cookie cookie = mockMvc.perform(loginRequest(email, "123456"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(JwtCookieService.COOKIE_NAME);
        org.assertj.core.api.Assertions.assertThat(cookie).isNotNull();
        return cookie;
    }

    private Aluno alunoAutenticado() {
        return alunoRepository.findAll().stream()
                .filter(aluno -> aluno.getEmail().getValor().equals("aluno@teste.com"))
                .findFirst()
                .orElseThrow();
    }

    private Aluno novoOutroAluno() {
        return alunoRepository.save(new Aluno(
                null,
                "Outro Aluno",
                "outro.aluno@teste.com",
                passwordEncoder.encode("123456"),
                "24681357928",
                GeneroUsuario.NAO_INFORMADO,
                "1/1/2001",
                "CD5678",
                StatusAluno.CURSANDO
        ));
    }

    private Professor professorAutenticado() {
        return professorRepository.findAll().stream()
                .filter(professor -> professor.getEmail().getValor().equals("professor@teste.com"))
                .findFirst()
                .orElseThrow();
    }

    private Professor novoOutroProfessor() {
        return professorRepository.save(new Professor(
                null,
                "Outro Professor",
                "outro.professor@teste.com",
                passwordEncoder.encode("123456"),
                "12345678909",
                GeneroUsuario.NAO_INFORMADO,
                "1/1/1984",
                "Computacao",
                90.0,
                TipoEnsinoProfessor.AMBOS
        ));
    }

    private String cursoPayload(String nome, Long professorId) {
        return """
                {
                  "nome":"%s",
                  "tipoAssinatura":"COMUM",
                  "tipoCurso":"ASSINCRONO",
                  "professorId":%d
                }
                """.formatted(nome, professorId);
    }

    private String matriculaPayload(Long alunoId, Long cursoId, String status) {
        return """
                {
                  "status": "%s",
                  "alunoId": %d,
                  "cursoId": %d
                }
                """.formatted(status, alunoId, cursoId);
    }

    private Curso novoCursoAprovado(String nome, Professor professor) {
        Curso curso = new Curso(null, nome, CursoAssinatura.COMUM, TipoCurso.ASSINCRONO);
        curso.setProfessor(professor);
        curso.setStatus(StatusCurso.APROVADO);
        return cursoRepository.save(curso);
    }

    private Curso novoCursoEmAvaliacao(String nome, Professor professor) {
        Curso curso = new Curso(null, nome, CursoAssinatura.COMUM, TipoCurso.ASSINCRONO);
        curso.setProfessor(professor);
        curso.setStatus(StatusCurso.EM_AVALIACAO);
        return cursoRepository.save(curso);
    }

    private Matricula novaMatricula(Aluno aluno, Curso curso) {
        Matricula matricula = new Matricula(null, StatusMatricula.ATIVA);
        matricula.setAluno(aluno);
        matricula.setCurso(curso);
        return matricula;
    }

    private Cookie cookieCsrf(Cookie... existingCookies) throws Exception {
        MockHttpServletRequestBuilder request = get("/csrf");
        if (existingCookies.length > 0) request.cookie(existingCookies);
        Cookie cookie = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("XSRF-TOKEN");
        org.assertj.core.api.Assertions.assertThat(cookie).isNotNull();
        org.assertj.core.api.Assertions.assertThat(cookie.isHttpOnly()).isFalse();
        return cookie;
    }

    private void validarLoginInvalido(String email, String senha) throws Exception {
        mockMvc.perform(loginRequest(email, senha))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpServletRequestBuilder loginRequest(String email, String senha,
                                                       Cookie... existingCookies) throws Exception {
        Cookie csrfCookie = cookieCsrf(existingCookies);
        MockHttpServletRequestBuilder request = post("/login")
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}");
        if (existingCookies.length > 0) request.cookie(existingCookies);
        return request;
    }
}
