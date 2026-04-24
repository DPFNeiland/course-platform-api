package ananditos.sandraxandreia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // SecurityFilterChain e a cadeia de filtros do Spring Security.
    // Pense nela como um "porteiro" que intercepta as requisicoes HTTP.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // csrf() configura protecao CSRF.
                // Para APIs REST didaticas/testes, costuma-se desabilitar.
                // Em producao, isso deve ser analisado com cuidado.
                .csrf(csrf -> csrf.disable())

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

                        // authenticated() = exige autenticacao.
                        // Aqui estamos protegendo as rotas REST principais.
                        .requestMatchers("/api/**").authenticated()

                        // anyRequest() pega o que nao foi coberto acima.
                        .anyRequest().permitAll())

                // frameOptions() foi liberado para o console H2 funcionar no navegador.
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // httpBasic() ativa autenticacao HTTP Basic.
                // E um modo simples e bem didatico para testes com Postman, Insomnia e Swagger.
                .httpBasic(Customizer.withDefaults());

        // build() monta o objeto final da configuracao de seguranca.
        return http.build();
    }

    // UserDetailsService e o servico que fornece usuarios para autenticacao.
    // Nesta versao, usamos um usuario em memoria, sem tabela no banco.
    @Bean
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                // User.withUsername(...) cria um usuario de forma fluente.
                User.withUsername("admin")
                        // encode(...) criptografa a senha antes de armazenar.
                        .password(passwordEncoder.encode("123456"))
                        .roles("ADMIN")
                        .build()
        );
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
