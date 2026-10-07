# Vibe Barbearia — Testes (JUnit)

Projeto NetBeans (Java SE / Ant) com os **testes unitários** das regras de negócio do
projeto [`VibeBarbearia-Core`](../README.md). Etapa 7 do Projeto Integrador SENAC.

## Como funciona

- Este projeto fica **dentro** da pasta do repositório `VibeBarbearia-Core` e referencia o Core
  pelo caminho relativo `..` (`nbproject/project.properties`: `project.VibeBarbearia-Core=..`).
  Por isso funciona em qualquer computador, desde que a pasta seja mantida dentro do Core.
- Ao executar os testes, o NetBeans/Ant **compila primeiro o jar do Core**
  (`../dist/VibeBarbearia-Core.jar`) e depois os testes.
- JUnit 4.13.2 e Hamcrest 1.3 estão em `lib/` (não depende das bibliotecas do IDE).
- **Sem banco de dados:** os testes usam apenas cálculos puros e repositórios em memória.

## Como executar

**NetBeans**

1. *File → Open Project…* → selecionar a pasta `VibeBarbearia-Testes`
   (abrir também o `VibeBarbearia-Core` é opcional).
2. Todos os testes: botão direito no projeto → **Test**, ou **Alt+F6** (*Test Project*).
3. Uma classe: abrir o arquivo de teste → **Ctrl+F6** (*Test File*).
4. O resultado aparece na janela *Test Results*.

**Linha de comando** (JDK 17+ e Apache Ant):

```bash
cd VibeBarbearia-Testes
ant test
# uma classe só:
ant -Djavac.includes=vibebarbearia/core/service/regras/ComissaoPercentualBarbeiroTest.java \
    -Dtest.includes=vibebarbearia/core/service/regras/ComissaoPercentualBarbeiroTest.java test-single
```

Resultado esperado: 12 classes, **44 testes, 0 falhas** (`BUILD SUCCESSFUL`).

## Testes

| Classe | Testes | O que verifica |
|---|---|---|
| `service.regras.ComissaoPercentualBarbeiroTest` | 4 | Comissão: R$ 85,00 × 40% = R$ 34,00; arredondamento a 2 casas |
| `service.regras.FidelidadePorValorTest` | 3 | 1 ponto a cada R$ 10,00 (R$ 85 → 8 pontos) |
| `service.regras.DescontoFidelidadeTest` | 5 | Regra nova: 100 pontos = R$ 10 de desconto, limite de 50% |
| `service.regras.RegraConflitoHorarioTest` | 4 | Intervalo mínimo de 30 min por barbeiro; remarcação |
| `service.ResumoCaixaTest` | 3 | Total, total por forma de pagamento, comissão, saldo, ticket médio |
| `service.AtendimentoServiceTest` | 3 | Finalização do atendimento com repositórios em memória |
| `util.PeriodoTest` | 4 | Validação e contagem de dias do período (ano bissexto) |
| `util.ConversorEntradaTest` | 5 | Conversão de valores, percentuais, horas e datas digitadas |
| `validation.ValidadorTest` | 5 | Telefone (máscara, mínimo 10 dígitos), CPF, e-mail, comissão 0–100% |
| `validation.PoliticaSenhaTest` | 3 | Senha mínima, confirmação e senha diferente da atual |
| `model.AgendamentoTest` | 2 | `concluir()` e regra de não concluir duas vezes |
| `model.ClienteTest` | 3 | `adicionarPontos()` / `resgatarPontos()` |

## Defeito encontrado pelos testes

`ConversorEntradaTest.dataInexistenteNaoPodeSerAjustadaSilenciosamente` mostrou que
`"31/02/2026"` era aceito e convertido para **28/02/2026** sem aviso. Corrigido no Core
(`Formatador` com `ResolverStyle.STRICT`), commit `fix(core): datas inexistentes…`.
