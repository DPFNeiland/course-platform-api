package ananditos.sandraxandreia.service.validation;

public class cpfNormalizado {

    public static String normalizarCPF(String cpf) {
        return cpf == null ? null : cpf.trim().toLowerCase();
    }
}
