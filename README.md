# Vibe Barbearia — Core (regras de negócio)

## Status do projeto

**Em desenvolvimento** — Projeto Integrador SENAC, **Etapa 6** (refatoração, SOLID e separação das regras de negócio).

## Objetivo

Este projeto NetBeans (Java SE / Ant) contém **apenas as regras de negócio** do sistema
*Vibe Barbearia*, separadas das telas Swing do projeto desktop (`VibeBarbearia`).
Ele **não depende de Swing/AWT** e poderá ser reaproveitado, sem alterações, por
uma futura versão **web** (por exemplo, Spring Boot ou Servlets) e pelo próprio desktop.

## Tecnologias

- Java 17+ (testado com OpenJDK 21)
- Apache NetBeans (projeto Ant, mesma estrutura `nbproject/` do desktop)
- JDBC + MySQL (mesmo banco `sql/vibebarbearia.sql` do projeto desktop)
- Git

## Arquitetura (camadas)

```
src/vibebarbearia/core/
├── model/             Entidades e enums (Cliente, Barbeiro, Agendamento, MovimentoCaixa,
│                      Usuario, Servico, FormaPagamento, ResultadoRemocaoBarbeiro...)
├── exception/         RegraNegocioException, ValidacaoException, AcessoNegadoException...
├── validation/        RegrasNegocio (constantes), Validador, PoliticaSenha
├── util/              Formatador, ConversorEntrada, TextoUtil, Periodo
├── repository/        Interfaces (ClienteRepository, AgendamentoRepository...) + RepositoryFactory
│   ├── memoria/       Implementações em memória (testes, sem banco)
│   └── jdbc/          Implementações MySQL (JdbcExecutor, ConexaoMySQL, *RepositoryJdbc)
├── service/           ClienteService, FuncionarioService, AgendamentoService,
│   │                  AtendimentoService, CaixaService, AutenticacaoService,
│   │                  CatalogoService, DashboardService
│   ├── regras/        PoliticaAcesso, PoliticaComissao, PoliticaFidelidade,
│   │                  CodificadorSenha, GeradorSenha, NotificadorSenha (Strategies)
│   └── relatorio/     RelatorioService + 6 estratégias de relatório
└── app/               VibeBarbeariaCore (composition root), TesteRegrasNegocio (main com testes)
```

## Como executar os testes (sem banco de dados)

**NetBeans:** abrir a pasta `VibeBarbearia-Core` → *Run Project* (F6).
A classe principal é `vibebarbearia.core.app.TesteRegrasNegocio`.

**Linha de comando:**

```bash
ant run
# ou
mkdir -p build/classes
javac -d build/classes $(find src -name "*.java")
java -cp build/classes vibebarbearia.core.app.TesteRegrasNegocio
```

Saída esperada: `Total: 49 | PASS: 49 | FAIL: 0`.

## Usando com MySQL

1. Criar o banco com `sql/vibebarbearia.sql` do projeto desktop.
2. Copiar `config/banco.properties.exemplo` para `config/banco.properties` e informar a senha
   (ou definir `VIBE_DB_URL`, `VIBE_DB_USUARIO`, `VIBE_DB_SENHA`).
3. No código cliente: `VibeBarbeariaCore core = VibeBarbeariaCore.comMySQL(notificador);`

## Exemplo de uso por uma tela (Swing ou Web)

```java
try {
    ResultadoAtendimento r = core.atendimento()
        .finalizar(usuarioLogado, idAgendamento, itensSelecionados, FormaPagamento.PIX);
    mostrarMensagem("Atendimento concluído", "Pontos ganhos: " + r.pontosGanhos());
} catch (RegraNegocioException e) {
    mostrarMensagem("Atenção", e.getMessage());
}
```

## Time

| Nome | Função |
|------|--------|
| Lucas Labes | Desenvolvimento, refatoração e versionamento |

---
*Projeto Integrador — Etapa 6 (Refatoração e SOLID)*
