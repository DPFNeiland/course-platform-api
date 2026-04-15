# 🎓 Sistema de Gamificação para Plataforma de Educação Continuada
 
> Projeto integrado das disciplinas de **Engenharia de Software** e **Programação Orientada a Objetos** — Centro Universitário Facens
 
---
 
## 👥 Integrantes
 
| Nome |
|------|
| Betina Volpi |
| Ícaro Dias |
| Kayla Abreu |
| Rodrigo Neiland |
 
---
 
## 📋 Sobre o Projeto
 
Uma plataforma de cursos online no modelo de assinaturas com sistema de gamificação para engajamento de alunos. O aluno paga uma mensalidade e acessa um conjunto de cursos; ao concluir cursos com média acima de 7,0 desbloqueia benefícios progressivos, chegando ao plano **Premium** ao conquistar 12 cursos — com acesso a vouchers, projetos reais e moedas convertíveis em novos cursos ou criptomoeda.
 
Este repositório abriga a implementação em Java/Spring Boot da **camada de domínio e API REST**, desenvolvida na disciplina de POO com base nos artefatos de engenharia de requisitos (ESW).
 
---
 
## 🏗️ Arquitetura
 
O projeto segue os princípios de **Clean Architecture** e **DDD (Domain Driven Design)**, com as seguintes camadas:
 
```
src/
└── main/
    └── java/
        └── com/grupo/gamificacao/
            ├── domain/          # Entidades e regras de negócio (POJOs → Entities JPA)
            ├── application/     # Casos de uso e serviços de aplicação
            ├── infrastructure/  # Repositórios Spring Data, configurações JPA
            └── presentation/    # Controllers REST (API)
```
 
### Princípios aplicados
- **Baixo Acoplamento** entre camadas
- **Alta Coesão** dentro de cada classe
- **ORM** via JPA/Hibernate com banco H2 em memória
- **Spring Data** para persistência
---
 
## 🛠️ Tecnologias
 
| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Java | 17+ | Linguagem principal |
| Spring Boot | 3.x | Framework base |
| Spring Data JPA | — | ORM / persistência |
| Hibernate | — | Implementação JPA |
| H2 Database | — | Banco em memória (dev/testes) |
| Maven | — | Gerenciamento de dependências |
| IntelliJ IDEA Ultimate | — | IDE recomendada |
 
---

## 📚 Referências
 
- GUEDES, G. T. A. *UML 2 – Guia Prático*. 2. ed. São Paulo: Novatec, 2011.
- GUEDES, G. T. A. *UML 2: uma abordagem prática*. 3. ed. São Paulo: Novatec, 2018.
- PRESSMAN, R. S.; MAXIM, B. R. *Engenharia de Software: uma abordagem profissional*. 9. ed. Porto Alegre: Bookman, 2021.
- SOMMERVILLE, I. *Engenharia de Software*. 10. ed. São Paulo: Pearson, 2019.
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MVN Repository](https://mvnrepository.com/)
