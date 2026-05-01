package ananditos.sandraxandreia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Camada: WEB / CONTROLLER SUPPORT.
// @RestControllerAdvice funciona como um tratamento global de excecoes para a API.
// Em vez de cada controller tratar erro manualmente, centralizamos aqui.
@RestControllerAdvice
public class ApiExceptionHandler {

    // @ExceptionHandler(RuntimeException.class) captura qualquer RuntimeException
    // lancada durante o fluxo da requisicao.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erro", ex.getMessage());

        // ResponseEntity permite controlar o status HTTP e o corpo da resposta.
        return ResponseEntity.badRequest().body(body);
    }

    // Este metodo trata erros de validacao disparados pelo @Valid nos controllers.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        Map<String, String> campos = new HashMap<>();

        // getBindingResult() traz o resultado das validacoes do DTO.
        // getFieldErrors() retorna os erros por campo.
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

        body.put("erros", campos);
        return ResponseEntity.badRequest().body(body);
    }
}
