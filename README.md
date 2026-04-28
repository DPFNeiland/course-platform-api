
# 🎓 Sistema de Gamificação para Plataforma de Educação Continuada
 
> Projeto integrado das disciplinas de **Engenharia de Software** e **Programação Orientada a Objetos** — Centro Universitário Facens
 
Uma plataforma de cursos online no modelo de assinaturas com sistema de gamificação para engajamento de alunos. O aluno paga uma mensalidade e acessa um conjunto de cursos; ao concluir cursos com média acima de 7,0 desbloqueia benefícios progressivos, chegando ao plano **Premium** ao conquistar 12 cursos — com acesso a vouchers, projetos reais e moedas convertíveis em novos cursos ou criptomoeda.
 
Este repositório abriga a implementação em Java/Spring Boot da **camada de domínio e API REST**, desenvolvida na disciplina de POO com base nos artefatos de engenharia de requisitos (ESW).

---

## Integrantes

- Betina Volpi
- Icaro Dias
- Kayla Abreu
- Rodrigo Neiland
--- 
## 📋 Sobre o Projeto
Projeto academico desenvolvido para modelar a camada de dominio e a API REST de uma plataforma de educacao continuada por assinatura.

A ideia inicial do sistema inclui cursos online, assinatura recorrente e mecanismos de progressao do aluno. Na evolucao atual do codigo, o foco principal ja esta na implementacao da API com Spring Boot, persistencia em H2/JPA e organizacao do dominio em entidades, DTOs, repositories, services e controllers.


---

## Arquitetura do projeto

Estrutura principal do codigo:

```text
src/
|- main/
|  |- java/ananditos/sandraxandreia/
|  |  |- config/        # configuracoes, incluindo seguranca
|  |  |- controller/    # endpoints REST
|  |  |- domain/        # entidades, enums e value objects
|  |  |- dto/
|  |  |  |- request/    # payloads de entrada
|  |  |  `- response/   # payloads de saida
|  |  |- repository/    # interfaces JpaRepository
|  |  `- service/       # regras de aplicacao e orquestracao
|  `- resources/
|     `- application.properties
`- test/
   `- java/ananditos/sandraxandreia/
```
### Princípios aplicados
- **Baixo Acoplamento** entre camadas
- **Alta Coesão** dentro de cada classe
- **ORM** via JPA/Hibernate com banco H2 em memória
- **Spring Data** para persistência

---

## Tecnologias e configuracao

| Item | Valor atual |
|------|-------------|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Persistencia | Spring Data JPA |
| Banco de dados | H2 em memoria |
| Seguranca | Spring Security |
| Documentacao da API | springdoc OpenAPI + Swagger UI |
| Build | Maven Wrapper |


---

## Recursos de apoio

### Swagger UI

- `http://localhost:8081/swagger-ui/index.html`

### H2 Console

- `http://localhost:8081/h2-console`

---
## Endpoints disponiveis

| Recurso | Rota base | Operacoes |
|---------|-----------|-----------|
| Usuario | `/usuario` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Aluno | `/aluno` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Professor | `/professor` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Curso | `/curso` | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |

---
## Referencias

- Spring Boot Documentation
- Spring Data JPA Documentation
- springdoc OpenAPI
- H2 Database Engine
- GUEDES, G. T. A. *UML 2 – Guia Prático*. 2. ed. São Paulo: Novatec, 2011.
- GUEDES, G. T. A. *UML 2: uma abordagem prática*. 3. ed. São Paulo: Novatec, 2018.
- PRESSMAN, R. S.; MAXIM, B. R. *Engenharia de Software: uma abordagem profissional*. 9. ed. Porto Alegre: Bookman, 2021.
- SOMMERVILLE, I. *Engenharia de Software*. 10. ed. São Paulo: Pearson, 2019.
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MVN Repository](https://mvnrepository.com/)