package ananditos.sandraxandreia;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.aluno.StatusAluno;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.TipoEnsinoProfessor;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.repository.AlunoRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private PasswordEncoder passwordEncoder;

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
                "52998224725",
                passwordEncoder.encode("123456"),
                "1/1/2000",
                GeneroUsuario.MASCULINO,
                "AB1234",
                StatusAluno.CURSANDO
        ));

        professorRepository.save(new Professor(
                null,
                "Professor Teste",
                "professor@teste.com",
                "39053344705",
                passwordEncoder.encode("123456"),
                "1/1/1985",
                GeneroUsuario.FEMININO,
                "Engenharia de Software",
                100.0,
                TipoEnsinoProfessor.AMBOS
        ));
    }

    @Test
    void devePermitirAlunoNoEndpointDeAluno() throws Exception {
        mockMvc.perform(get("/aluno").with(httpBasic("aluno@teste.com", "123456")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAlunoNoEndpointDeCurso() throws Exception {
        mockMvc.perform(get("/curso").with(httpBasic("aluno@teste.com", "123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirProfessorNoEndpointDeCurso() throws Exception {
        mockMvc.perform(get("/curso").with(httpBasic("professor@teste.com", "123456")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearProfessorNoEndpointDeMatricula() throws Exception {
        mockMvc.perform(get("/matricula").with(httpBasic("professor@teste.com", "123456")))
                .andExpect(status().isForbidden());
    }
}
