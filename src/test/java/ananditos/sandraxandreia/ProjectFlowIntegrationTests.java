package ananditos.sandraxandreia;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.aluno.StatusAluno;
import ananditos.sandraxandreia.domain.assinatura.Assinatura;
import ananditos.sandraxandreia.domain.curador.Curador;
import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.TipoEnsinoProfessor;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.repository.AlunoRepository;
import ananditos.sandraxandreia.repository.AssinaturaRepository;
import ananditos.sandraxandreia.repository.CuradorRepository;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.MaterialCursoRepository;
import ananditos.sandraxandreia.repository.MatriculaRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import ananditos.sandraxandreia.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
class ProjectFlowIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MaterialCursoRepository materialCursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private AssinaturaRepository assinaturaRepository;

    @Autowired
    private CuradorRepository curadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Aluno aluno;
    private Professor professor;
    private Curador curador;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        matriculaRepository.deleteAll();
        materialCursoRepository.deleteAll();
        cursoRepository.deleteAll();
        assinaturaRepository.deleteAll();
        alunoRepository.deleteAll();
        professorRepository.deleteAll();
        curadorRepository.deleteAll();
        usuarioRepository.deleteAll();

        aluno = alunoRepository.save(new Aluno(
                null,
                "Aluno Fluxo",
                "aluno.fluxo@teste.com",
                passwordEncoder.encode("123456"),
                "52998224725",
                GeneroUsuario.MASCULINO,
                "1/1/2000",
                "ZX1234",
                StatusAluno.CURSANDO
        ));

        professor = professorRepository.save(new Professor(
                null,
                "Professor Fluxo",
                "professor.fluxo@teste.com",
                passwordEncoder.encode("123456"),
                "39053344705",
                GeneroUsuario.FEMININO,
                "1/1/1985",
                "Engenharia de Software",
                100.0,
                TipoEnsinoProfessor.AMBOS
        ));

        curador = curadorRepository.save(new Curador(
                null,
                "Curador Fluxo",
                "curador.fluxo@teste.com",
                passwordEncoder.encode("123456"),
                "98765432100",
                GeneroUsuario.NAO_INFORMADO,
                "1/1/1990"
        ));
    }

    @Test
    void deveCadastrarCuradorEListarCuradores() throws Exception {
        String payload = """
                {
                  "nome": "Curador Novo",
                  "email": "novo.curador@teste.com",
                  "cpf": "24681357928",
                  "senha": "123456",
                  "genero": "NAO_INFORMADO",
                  "dataNascimento": "1/1/1992"
                }
                """;

        mockMvc.perform(post("/curador")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cargo").value("CURADOR"))
                .andExpect(jsonPath("$.email").value("novo.curador@teste.com"));

        mockMvc.perform(get("/curador")
                        .header("Authorization", bearer(curador.getEmail().getValor())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='novo.curador@teste.com')]").exists());
    }

    @Test
    void deveCriarAssinaturaComCuradorEListarParaAluno() throws Exception {
        String payload = """
                {
                  "nome": "Plano Basico",
                  "assinatura": "COMUM",
                  "preco": 0.0,
                  "beneficios": ["catalogo base", "acesso imediato"]
                }
                """;

        mockMvc.perform(post("/assinatura")
                        .header("Authorization", bearer(curador.getEmail().getValor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Plano Basico"))
                .andExpect(jsonPath("$.assinatura").value("COMUM"));

        mockMvc.perform(get("/assinatura")
                        .header("Authorization", bearer("aluno.fluxo@teste.com"))
                        .param("plano", "COMUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Plano Basico"));
    }

    @Test
    void deveRetornarBadRequestQuandoAssinaturaForInvalida() throws Exception {
        String payload = """
                {
                  "nome": "",
                  "preco": -5,
                  "beneficios": []
                }
                """;

        mockMvc.perform(post("/assinatura")
                        .header("Authorization", bearer(curador.getEmail().getValor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.beneficios").exists());
    }

    @Test
    void devePermitirProfessorAdicionarLinkEmCursoProprio() throws Exception {
        Curso curso = new Curso(null, "Curso Com Link", CursoAssinatura.COMUM, TipoCurso.ASSINCRONO);
        curso.setProfessor(professor);
        curso = cursoRepository.save(curso);

        String payload = """
                {
                  "titulo": "Aula 1",
                  "url": "https://example.com/aula-1"
                }
                """;

        mockMvc.perform(post("/curso/{cursoId}/materiais/link", curso.getId())
                        .header("Authorization", bearer(professor.getEmail().getValor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("LINK"))
                .andExpect(jsonPath("$.titulo").value("Aula 1"));
    }

    @Test
    void devePermitirProfessorEnviarArquivoEmCursoProprioEBaixarDepois() throws Exception {
        Curso curso = new Curso(null, "Curso Com Arquivo", CursoAssinatura.PREMIUM, TipoCurso.SINCRONO);
        curso.setProfessor(professor);
        curso = cursoRepository.save(curso);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "roteiro.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "conteudo da aula".getBytes()
        );

        MvcResult resultado = mockMvc.perform(multipart("/curso/{cursoId}/materiais/arquivo", curso.getId())
                        .file(arquivo)
                        .param("titulo", "Roteiro da aula")
                        .header("Authorization", bearer(professor.getEmail().getValor())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ARQUIVO"))
                .andExpect(jsonPath("$.nomeArquivo").value("roteiro.txt"))
                .andReturn();

        String corpo = resultado.getResponse().getContentAsString();
        Long materialId = new ObjectMapper().readTree(corpo).get("id").asLong();

        mockMvc.perform(get("/curso/{cursoId}/materiais/{materialId}/arquivo", curso.getId(), materialId)
                        .header("Authorization", bearer(professor.getEmail().getValor())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("roteiro.txt")))
                .andExpect(content().bytes("conteudo da aula".getBytes()));
    }

    @Test
    void deveBloquearProfessorAdicionarMaterialEmCursoDeOutroProfessor() throws Exception {
        Professor outroProfessor = professorRepository.save(new Professor(
                null,
                "Professor Visitante",
                "visitante.professor@teste.com",
                passwordEncoder.encode("123456"),
                "31415926590",
                GeneroUsuario.MASCULINO,
                "1/1/1988",
                "Tecnologia",
                80.0,
                TipoEnsinoProfessor.AMBOS
        ));

        Curso curso = new Curso(null, "Curso Privado", CursoAssinatura.COMUM, TipoCurso.ASSINCRONO);
        curso.setProfessor(professor);
        curso = cursoRepository.save(curso);

        String payload = """
                {
                  "titulo": "Aula 2",
                  "url": "https://example.com/aula-2"
                }
                """;

        mockMvc.perform(post("/curso/{cursoId}/materiais/link", curso.getId())
                        .header("Authorization", bearer(outroProfessor.getEmail().getValor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Professor nao pode alterar materiais de curso que nao pertence a ele"));
    }

    @Test
    void deveImpedirMatriculaEmCursoNaoAprovado() throws Exception {
        Curso curso = new Curso(null, "Curso Em Avaliacao", CursoAssinatura.COMUM, TipoCurso.SINCRONO);
        curso.setProfessor(professor);
        curso = cursoRepository.save(curso);

        String payload = """
                {
                  "status": "ATIVA",
                  "alunoId": %d,
                  "cursoId": %d
                }
                """.formatted(aluno.getId(), curso.getId());

        mockMvc.perform(post("/matricula")
                        .header("Authorization", bearer("aluno.fluxo@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Somente cursos aprovados podem receber matriculas"));
    }

    @Test
    void deveImpedirMatriculaDuplicadaNoMesmoCurso() throws Exception {
        Curso curso = new Curso(null, "Curso Aprovado", CursoAssinatura.COMUM, TipoCurso.ASSINCRONO);
        curso.setProfessor(professor);
        curso.setStatus(StatusCurso.APROVADO);
        curso = cursoRepository.save(curso);

        String payload = """
                {
                  "status": "ATIVA",
                  "alunoId": %d,
                  "cursoId": %d
                }
                """.formatted(aluno.getId(), curso.getId());

        mockMvc.perform(post("/matricula")
                        .header("Authorization", bearer("aluno.fluxo@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cursoId").value(curso.getId()));

        mockMvc.perform(post("/matricula")
                        .header("Authorization", bearer("aluno.fluxo@teste.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Aluno ja matriculado nesse curso"));
    }

    @Test
    void deveBloquearDuplicidadeDeMatriculaTambemNoBanco() {
        Curso curso = new Curso(null, "Curso Unico", CursoAssinatura.PREMIUM, TipoCurso.SINCRONO);
        curso.setProfessor(professor);
        curso.setStatus(StatusCurso.APROVADO);
        curso = cursoRepository.save(curso);

        Matricula primeira = new Matricula(null, StatusMatricula.ATIVA);
        primeira.setAluno(aluno);
        primeira.setCurso(curso);
        matriculaRepository.saveAndFlush(primeira);

        Matricula duplicada = new Matricula(null, StatusMatricula.EM_ANDAMENTO);
        duplicada.setAluno(aluno);
        duplicada.setCurso(curso);

        assertThrows(DataIntegrityViolationException.class, () -> matriculaRepository.saveAndFlush(duplicada));
    }

    @Test
    void devePermitirCuradorAtualizarPlanoDaAssinatura() throws Exception {
        Assinatura assinatura = assinaturaRepository.save(new Assinatura(
                null,
                "Plano Evolucao",
                ananditos.sandraxandreia.domain.assinatura.PlanoAssinatura.COMUM,
                49.9,
                List.of("catalogo base")
        ));

        mockMvc.perform(patch("/assinatura/{id}/plano", assinatura.getId())
                        .header("Authorization", bearer(curador.getEmail().getValor()))
                        .param("novoPlano", "PREMIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assinatura").value("PREMIUM"));
    }

    private String bearer(String email) {
        return "Bearer " + jwtService.emitir(email).token();
    }
}
