package ananditos.sandraxandreia.service.validation;

public class cpfNormalizado {

    public static String normalizarCPF(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }
}
