package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.TipoEnsinoProfessor;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import ananditos.sandraxandreia.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class CursoControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private JwtService jwtService;

    private Professor professor;

    @BeforeEach
    void setUp() {
        professor = new Professor(
                null,
                "Rodrigo",
                "rodrigo@teste.com",
                "123456",
                "12345678909",
                GeneroUsuario.MASCULINO,
                "10/08/1995",
                "Informatica",
                50.0,
                TipoEnsinoProfessor.SINCRONO
        );
        professor = professorRepository.save(professor);
    }

    @Test
    void postCursoComProfessorIdSalvaVinculoCursoProfessor() throws Exception {
        String payload = """
                {
                  "nome": "Java para Iniciantes",
                  "tipoAssinatura": "PREMIUM",
                  "tipoCurso": "SINCRONO",
                  "professorId": %d
                }
                """.formatted(professor.getId());

        mockMvc.perform(post("/curso")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Java para Iniciantes"))
                .andExpect(jsonPath("$.status").value("EM_AVALIACAO"))
                .andExpect(jsonPath("$.professorId").value(professor.getId()));

        Curso salvo = cursoRepository.findAll().stream()
                .filter(curso -> curso.getNome().equals("Java para Iniciantes"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Curso nao foi persistido"));

        assertThat(salvo.getProfessor()).isNotNull();
        assertThat(salvo.getProfessor().getId()).isEqualTo(professor.getId());
    }

    @Test
    void postCursoComTypoProfessorIidNaoVinculaNemSalva() throws Exception {
        String payload = """
                {
                  "nome": "Curso Com Typo",
                  "tipoAssinatura": "COMUM",
                  "tipoCurso": "AMBOS",
                  "professorIid": %d
                }
                """.formatted(professor.getId());

        mockMvc.perform(post("/curso")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        assertThat(cursoRepository.findAll())
                .noneMatch(curso -> curso.getNome().equals("Curso Com Typo"));
    }

    @Test
    void postCursoSemProfessorIdRetornaBadRequest() throws Exception {
        String payload = """
                {
                  "nome": "Curso Sem Professor",
                  "tipoAssinatura": "COMUM",
                  "tipoCurso": "AMBOS"
                }
                """;

        mockMvc.perform(post("/curso")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        assertThat(cursoRepository.findAll())
                .noneMatch(curso -> curso.getNome().equals("Curso Sem Professor"));
    }

    @Test
    void getCursosPorProfessorRetornaSomenteCursosDoProfessorInformado() throws Exception {
        Professor outroProfessor = professorRepository.save(new Professor(
                null,
                "Outro Professor",
                "outro-professor@teste.com",
                "123456",
                "98765432100",
                GeneroUsuario.MASCULINO,
                "11/09/1990",
                "Matematica",
                45.0,
                TipoEnsinoProfessor.ASSINCRONO
        ));

        Curso cursoProprio = new Curso(null, "Curso da Agenda", CursoAssinatura.COMUM, TipoCurso.SINCRONO);
        cursoProprio.setProfessor(professor);
        cursoRepository.save(cursoProprio);

        Curso cursoDeOutroProfessor = new Curso(null, "Curso de Outro Professor", CursoAssinatura.PREMIUM, TipoCurso.ASSINCRONO);
        cursoDeOutroProfessor.setProfessor(outroProfessor);
        cursoRepository.save(cursoDeOutroProfessor);

        mockMvc.perform(get("/curso/professor/{professorId}", professor.getId())
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Curso da Agenda"))
                .andExpect(jsonPath("$[0].professorId").value(professor.getId()))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    private String bearer() {
        return "Bearer " + jwtService.emitir(professor.getEmail().getValor()).token();
    }
}
