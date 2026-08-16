# Decisão — armazenamento seguro do token de sessão

## Estratégia escolhida

O JWT de autenticação será armazenado em **cookie `HttpOnly`**, e não no `sessionStorage`.

O `sessionStorage` mantém apenas informações não sensíveis usadas para renderização e navegação, como identificador, nome, perfil e horário de expiração. O JWT não é retornado no JSON de login e não fica acessível ao JavaScript ou a `document.cookie`.

## Configuração do cookie

O backend grava o cookie `SXA_SESSION` com:

- `HttpOnly`, impedindo acesso por JavaScript;
- `SameSite=Strict`, reduzindo o risco de CSRF;
- `Path=/`;
- `Max-Age` igual à validade do JWT;
- `Secure` configurável e obrigatório em ambientes HTTPS.

Configurações:

```text
JWT_SECRET=<segredo com pelo menos 32 caracteres>
JWT_EXPIRATION_SECONDS=3600
JWT_COOKIE_SECURE=true
```

Para desenvolvimento local exclusivamente em HTTP, `JWT_COOKIE_SECURE` deve ser `false`. Em produção com HTTPS, deve permanecer `true`.

## Autenticação das requisições

O frontend envia:

```javascript
credentials: 'include'
```

O navegador inclui o cookie automaticamente. Nenhum módulo frontend lê, armazena ou monta um header com o JWT.

O CORS permanece limitado às origens explicitamente autorizadas e usa `allowCredentials=true`. Não é usado curinga em `Allowed-Origin`.

## Expiração e renovação

Não foi implementado refresh token nesta entrega.

A sessão expira ao final de `JWT_EXPIRATION_SECONDS`. Quando isso acontece:

1. o cookie expira no navegador;
2. o backend rejeita o JWT expirado com `401` e código `TOKEN_EXPIRED`;
3. o frontend remove os dados visuais da sessão;
4. o usuário é redirecionado e deve autenticar-se novamente.

Essa decisão evita introduzir um token de longa duração sem infraestrutura de rotação e revogação.

## Logout

`POST /logout` responde com `204 No Content` e sobrescreve `SXA_SESSION` com `Max-Age=0`, removendo o cookie no navegador. O logout padrão com redirecionamento do Spring Security foi desabilitado para preservar o contrato REST.

Como a arquitetura permanece stateless e não possui blacklist, uma cópia roubada do JWT continuaria válida até expirar. Revogação antecipada fica como evolução futura caso o risco do produto exija.

## Proteção contra CSRF

O token agora é enviado por cookie, por isso a decisão considera CSRF explicitamente:

- `SameSite=Strict` impede o envio normal do cookie em contextos entre sites;
- CORS permite credentials somente para a lista explícita de origens do frontend;
- a API não usa `Access-Control-Allow-Origin: *` com credenciais.

Se frontend e backend passarem a operar em sites diferentes, a estratégia deverá ser reavaliada antes de alterar `SameSite` para `None`.

## Validações automatizadas

- login grava cookie com `HttpOnly` e `SameSite=Strict`;
- o JSON de login não contém o JWT;
- cookie retornado pelo login autentica aluno, professor e curador;
- logout retorna `204` e remove o cookie com `Max-Age=0`;
- token expirado e inválido continuam retornando `401`;
- frontend não contém `btoa`, token em sessão ou header Bearer montado por JavaScript;
- todos os clientes autenticados utilizam `credentials: 'include'`.
