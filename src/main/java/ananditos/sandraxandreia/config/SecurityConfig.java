package ananditos.sandraxandreia.config;

import ananditos.sandraxandreia.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private static final List<String> PUBLIC_POST_PATHS = List.of("/login", "/aluno", "/professor", "/curador");
    private static final List<String> SAFE_METHODS = List.of("GET", "HEAD", "TRACE", "OPTIONS");

    // SecurityFilterChain e a cadeia de filtros do Spring Security.
    // Pense nela como um "porteiro" que intercepta as requisicoes HTTP.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(cookieClientCsrfMatcher()))

                .cors(Customizer.withDefaults())

                // authorizeHttpRequests() define quem pode acessar cada rota.
                .authorizeHttpRequests(auth -> auth
                        // requestMatchers(...) lista rotas especificas.
                        // permitAll() = qualquer pessoa pode acessar, sem login.
                        .requestMatchers(
                                "/", "/index.html", "/home",
                                "/css/**", "/js/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()

                        // Cadastro inicial e login permanecem publicos.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login", "/aluno", "/professor", "/curador").permitAll()

                        // anyRequest() pega o que nao foi coberto acima.
                        .anyRequest().authenticated())

                // frameOptions() foi liberado para o console H2 funcionar no navegador.
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"status\":401,\"erro\":\"Autenticacao necessaria\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            String codigo = exception instanceof CsrfException ? "CSRF_INVALID" : "ACCESS_DENIED";
                            response.getWriter().write("{\"status\":403,\"codigo\":\"" + codigo
                                    + "\",\"erro\":\"Acesso negado\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // build() monta o objeto final da configuracao de seguranca.
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${security.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private RequestMatcher cookieClientCsrfMatcher() {
        return request -> {
            String method = request.getMethod();
            if (SAFE_METHODS.contains(method)) return false;

            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) return false;

            return !("POST".equals(method) && PUBLIC_POST_PATHS.contains(request.getRequestURI()));
        };
    }

    // PasswordEncoder e um bean importante da aplicacao.
    // Ele sera injetado na camada de service.
    // Observe um ponto didatico central:
    // - o bean nasce aqui, na camada de configuracao
    // - ele e consumido/injetado na camada de servico
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder e uma implementacao segura para hash de senha.
        return new BCryptPasswordEncoder();
    }
}
