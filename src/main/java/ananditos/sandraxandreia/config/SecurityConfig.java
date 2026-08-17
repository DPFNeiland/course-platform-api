package ananditos.sandraxandreia.config;

import ananditos.sandraxandreia.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // SecurityFilterChain e a cadeia de filtros do Spring Security.
    // Pense nela como um "porteiro" que intercepta as requisicoes HTTP.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(request -> {
                            String method = request.getMethod();
                            if (List.of("GET", "HEAD", "TRACE", "OPTIONS").contains(method)) {
                                return false;
                            }

                            String authorization = request.getHeader("Authorization");
                            if (authorization != null && authorization.startsWith("Bearer ")) {
                                return false;
                            }

                            String path = request.getRequestURI();
                            return !("POST".equals(method) && List.of("/login", "/aluno", "/professor", "/curador").contains(path));
                        }))

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
                            response.getWriter().write("{\"status\":403,\"erro\":\"Acesso negado\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // build() monta o objeto final da configuracao de seguranca.
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173", "http://127.0.0.1:5173",
                "http://localhost:5500", "http://127.0.0.1:5500",
                "http://localhost:3000", "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
