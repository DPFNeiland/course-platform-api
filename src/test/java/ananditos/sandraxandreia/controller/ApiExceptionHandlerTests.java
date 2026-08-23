package ananditos.sandraxandreia.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTests {

    @Test
    void conflitoDeBancoNaoDeveExporSqlOuConstraint() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        var response = handler.handleDataIntegrity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("erro", "Os dados informados ja estao cadastrados");
        assertThat(response.getBody().toString())
                .doesNotContain("constraint", "insert into", "uk_");
    }
}
