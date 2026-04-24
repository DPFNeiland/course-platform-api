package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.GeneroUsuario;
import ananditos.sandraxandreia.domain.TipoEnsinoProfessor;

public class ProfessorResponseDTO extends UsuarioResponseDTO {

    private String areaFormacao;
    private double horaAula;
    private TipoEnsinoProfessor tipoEnsino;

    public ProfessorResponseDTO() {
    }

    public ProfessorResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, String areaFormacao, double horaAula, TipoEnsinoProfessor tipoEnsino) {
        super(id, nome, email, cpf, genero);
        this.areaFormacao = areaFormacao;
        this.horaAula = horaAula;
        this.tipoEnsino = tipoEnsino;
    }

    public String getAreaFormacao() {
        return areaFormacao;
    }

    public double getHoraAula() {
        return horaAula;
    }

    public TipoEnsinoProfessor getTipoEnsino() {
        return tipoEnsino;
    }
}
