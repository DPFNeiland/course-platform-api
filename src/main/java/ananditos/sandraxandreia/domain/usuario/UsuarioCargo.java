package ananditos.sandraxandreia.domain.usuario;

public enum UsuarioCargo {
    ADMIN("ROLE_ADMIN"),
    PROFESSOR("ROLE_PROFESSOR"),
    ALUNO("ROLE_ALUNO"),
    CURADOR("ROLE_CURADOR"),
    ANONIMO("ROLE_ANONIMO");

    private final String role;

    UsuarioCargo(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
