package ananditos.sandraxandreia.service.validation;



public class emailNormalizado {
    public static String normalizarEMAIL(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
