# SXA-10 — Relatório de Code Review da migração para JWT

## Contexto

A task migra a autenticação baseada em `Basic Auth`, que armazenava `btoa(email:senha)` no `sessionStorage`, para JWT enviado pelo header `Authorization: Bearer {token}`.

O backend deve emitir o token e sua expiração no login, validar o JWT nas rotas protegidas e rejeitar tokens ausentes, inválidos ou expirados. O frontend deve usar Bearer nos fluxos de aluno, professor e curador, limpar sessões inválidas e redirecionar o usuário ao login.

## Veredito do Code Review

**Precisa de ajustes antes da aprovação.**

A implementação principal está próxima do critério de aceite e a suíte local passou com 32 testes. Entretanto, ainda existem bloqueadores relacionados ao estado do Git, à limpeza de credenciais legadas e à documentação incompatível com a estratégia implementada.

## Problemas de severidade alta

### 1. Alterações finais ainda não commitadas

Os ajustes finais para JWT Bearer estão somente no working tree. Um Pull Request real considera apenas alterações commitadas e enviadas ao repositório remoto.

**Correção prevista:**

- revisar o diff final;
- garantir que não existam arquivos acidentalmente alterados;
- criar o commit da correção;
- enviar a branch antes de solicitar nova revisão.

### 2. Credenciais antigas podem permanecer no navegador

A implementação anterior gravava dados nas chaves `session` e `user`. A implementação atual utiliza somente `session`, mas remove apenas essa chave no logout.

Uma aba que já tenha utilizado a versão Basic Auth pode conservar a chave legada `user`, inclusive com o antigo conteúdo Base64 de `email:senha`.

**Correção prevista:**

- remover `user` durante a inicialização do gerenciador de sessão;
- remover `user` depois de um login bem-sucedido;
- remover `user` no logout e em qualquer invalidação de sessão;
- adicionar teste que garanta a remoção da chave legada.

### 3. Documentação incompatível com o código

O documento `decisoes/SXA-validacao-armazenamento-token.md` descreve autenticação por cookie `HttpOnly`, `credentials: 'include'`, endpoint de logout e ausência do JWT no JSON de login.

A implementação da SXA-10 utiliza JWT retornado no corpo do login, armazenado no `sessionStorage` e enviado como Bearer.

**Correção prevista:**

- retirar o documento de cookie desta branch, caso pertença a outra task; ou
- atualizar claramente o documento para informar que ele representa uma decisão futura e ainda não implementada;
- manter nesta branch apenas documentação compatível com JWT Bearer.

## Problemas de severidade média

### 1. Ausência de teste comportamental do frontend

Os testes atuais verificam strings nos arquivos JavaScript, mas não executam o comportamento do navegador.

Ainda é necessário validar automaticamente:

- sessão expirada antes da chamada à API;
- resposta `401` com `TOKEN_EXPIRED`;
- resposta `401` com `TOKEN_INVALID`;
- limpeza das chaves `session` e `user`;
- redirecionamento para o login;
- logout nos três perfis.

**Correção prevista:** criar testes JavaScript mínimos para `session.js`, simulando `sessionStorage`, resposta HTTP e redirecionamento.

### 2. Duplicação do cliente HTTP autenticado

Os módulos de aluno, professor e curador repetem a montagem do header Bearer, o tratamento de `401`, a leitura de erros e a conversão da resposta.

**Correção prevista:** avaliar a criação de um cliente HTTP compartilhado. A refatoração deve ser feita somente se puder permanecer pequena e sem ampliar desnecessariamente o escopo da task.

### 3. Logout não revoga o JWT

O logout remove o token do navegador, mas uma cópia já obtida permanece válida até a expiração.

**Correção prevista:** documentar esse comportamento como limitação da estratégia stateless. Revogação ou refresh token ficam como evolução separada, salvo nova definição de requisito.

### 4. Testes de expiração dependem de espera real

Os testes utilizam `Thread.sleep(1100)`, o que aumenta o tempo da suíte e pode causar instabilidade.

**Correção prevista:** avaliar a injeção de `Clock` no `JwtService` para permitir testes determinísticos de emissão e expiração.

## Problemas de severidade baixa

### 1. Tratamento global pode expor mensagens internas

O tratamento genérico de `RuntimeException` devolve `ex.getMessage()` ao cliente. Algumas exceções podem expor detalhes internos da aplicação.

**Correção prevista:** retornar uma mensagem pública genérica para erros inesperados e manter detalhes somente nos logs do backend.

### 2. Contrato do frontend baseado em busca textual

Os testes de contrato procuram trechos exatos no código. Eles são úteis contra regressões simples, mas são frágeis diante de mudanças de formatação e não comprovam o comportamento em execução.

**Correção prevista:** manter o teste como proteção complementar e adicionar testes comportamentais para as regras principais.

## Pontos positivos identificados

- Basic Auth foi removido da configuração do Spring Security.
- A API utiliza sessão stateless.
- O login retorna JWT e data de expiração.
- O segredo JWT é externo à aplicação e exige tamanho mínimo.
- Tokens inválidos, expirados, malformados e assinados com outra chave são rejeitados.
- As regras de autorização por perfil permanecem ativas.
- Aluno, professor e curador enviam o header Bearer.
- O frontend trata respostas `401` e valida a expiração local.
- O cadastro autentica pelo endpoint `/login`, sem criar credenciais em Base64.
- Basic Auth é explicitamente rejeitado nos testes.
- A suíte local executou 32 testes sem falhas.

## Plano de implementação da correção

1. Corrigir a limpeza das chaves legadas do `sessionStorage`.
2. Ajustar ou remover a documentação incompatível com JWT Bearer.
3. Adicionar testes para limpeza, expiração, token inválido, logout e redirecionamento.
4. Avaliar a centralização do cliente HTTP sem ampliar o escopo.
5. Melhorar o tratamento de erros inesperados do backend.
6. Executar testes Java, testes do frontend e busca por `btoa`/Basic Auth.
7. Revisar o diff, criar o commit e enviar a branch.

## Critérios para nova aprovação

- nenhuma ocorrência de `btoa(email:senha)` no frontend;
- nenhuma chamada autenticada utilizando Basic Auth;
- aluno, professor e curador utilizando `Bearer {token}`;
- chaves legadas contendo credenciais removidas do navegador;
- token ausente, inválido ou expirado causando limpeza e redirecionamento;
- documentação alinhada com a implementação da branch;
- suíte completa aprovada;
- todas as alterações necessárias commitadas e enviadas ao remoto;
- ausência de alterações fora do escopo.

## Resultado da implementação

### Corrigido

- **Segurança / Alta:** remoção da chave legada `user` na inicialização, no login, no logout e na invalidação da sessão.
- **Arquitetura / Alta:** remoção da documentação de cookie `HttpOnly`, incompatível com a estratégia Bearer desta branch.
- **Teste / Média:** inclusão de testes comportamentais de sessão expirada, `TOKEN_EXPIRED`, `TOKEN_INVALID`, logout, limpeza das chaves e redirecionamento.
- **Teste / Média:** remoção de esperas reais dos testes de expiração por meio de um relógio injetável no serviço JWT.
- **Teste / Baixa:** manutenção da busca textual somente como proteção complementar aos testes comportamentais.

### Mantido por decisão de escopo

- **Arquitetura / Média:** a centralização dos três clientes HTTP foi adiada, pois não é necessária para o critério de aceite e ampliaria a refatoração deste PR.
- **Segurança / Média:** o logout permanece stateless e não revoga tokens copiados. O JWT continua válido até expirar; blacklist ou refresh token exigem decisão arquitetural própria.

### Dúvida registrada

- **Segurança / Baixa:** o handler de `RuntimeException` também transporta mensagens de validações de negócio já utilizadas pelo frontend. Sanitizá-lo indiscriminadamente quebraria mensagens funcionais. A separação entre exceções de negócio e erros inesperados deve ser tratada em uma refatoração específica.
