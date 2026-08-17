package ananditos.sandraxandreia.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

public final class SecurityRequestPolicy {
    private static final Set<String> PUBLIC_POST_PATHS = Set.of("/login", "/aluno", "/professor", "/curador");

    private SecurityRequestPolicy() {
    }

    public static boolean isPublicPost(String method, String path) {
        return "POST".equals(method) && PUBLIC_POST_PATHS.contains(path);
    }

    public static boolean isAuthenticationBootstrap(HttpServletRequest request) {
        return ("GET".equals(request.getMethod()) && "/csrf".equals(request.getRequestURI()))
                || isPublicPost(request.getMethod(), request.getRequestURI());
    }

    public static String[] publicPostPaths() {
        return PUBLIC_POST_PATHS.toArray(String[]::new);
    }
}
