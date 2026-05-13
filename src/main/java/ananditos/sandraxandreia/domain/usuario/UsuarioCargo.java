package ananditos.sandraxandreia.domain.usuario;

public enum UsuarioCargo {
    ADMIN("admin"),
    PROFESSOR("professor"),
    ALUNO("aluno"),
    CURADOR("curador"),
    ANONIMO("anonimo");

    private final String role;

    UsuarioCargo(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
