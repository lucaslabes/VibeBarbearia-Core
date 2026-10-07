package vibebarbearia.core.app;

import vibebarbearia.core.exception.AcessoNegadoException;
import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.exception.ValidacaoException;
import vibebarbearia.core.model.*;
import vibebarbearia.core.service.*;
import vibebarbearia.core.service.regras.SenhaSha256;
import vibebarbearia.core.service.relatorio.ResultadoRelatorio;
import vibebarbearia.core.service.relatorio.TipoRelatorio;
import vibebarbearia.core.util.ConversorEntrada;
import vibebarbearia.core.util.Formatador;
import vibebarbearia.core.util.Periodo;
import vibebarbearia.core.validation.Validador;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static vibebarbearia.core.app.MiniTestRunner.*;

/**
 * Classe principal do projeto VibeBarbearia-Core.
 * Executa, no main(), os testes de todas as regras de negócio usando os
 * repositórios EM MEMÓRIA (não precisa de MySQL).
 */
public class TesteRegrasNegocio {

    public static void main(String[] args) {
        System.out.println("VibeBarbearia-Core - Testes das regras de negócio (repositórios em memória)");
        MiniTestRunner r = new MiniTestRunner();
        testesValidacao(r);
        testesAcesso(r);
        testesClientes(r);
        testesFuncionarios(r);
        testesAgenda(r);
        testesAtendimento(r);
        testesCaixa(r);
        testesAutenticacao(r);
        testesDashboardRelatorios(r);
        int falhas = r.resumo();
        System.exit(falhas == 0 ? 0 : 1);
    }

    private static void testesValidacao(MiniTestRunner r) {
        r.grupo("Validação e conversão");
        r.teste("Telefone com máscara e 11 dígitos é aceito e normalizado", () ->
                igual("11988887777", Validador.telefone("(11) 98888-7777")));
        r.teste("Telefone com 9 dígitos é rejeitado", () ->
                lanca(ValidacaoException.class, () -> Validador.telefone("119888877")));
        r.teste("CPF precisa de exatamente 11 dígitos", () -> {
            igual("12345678901", Validador.cpf("123.456.789-01"));
            lanca(ValidacaoException.class, () -> Validador.cpf("1234567890"));
        });
        r.teste("E-mail sem '@' ou curto demais é inválido", () -> {
            verdadeiro(!Validador.isEmailValido("lucas.com"), "sem @ deveria ser inválido");
            verdadeiro(!Validador.isEmailValido("a@b"), "curto deveria ser inválido");
            verdadeiro(Validador.isEmailValido("a@b.co"), "a@b.co deveria ser válido");
        });
        r.teste("Comissão fora de 0..100 é rejeitada", () ->
                lanca(ValidacaoException.class, () -> Validador.comissao(150)));
        r.teste("Valor 'R$ 35,90' é convertido para 35.90", () ->
                igual(35.90, (double) ConversorEntrada.valorMonetario("R$ 35,90")));
        r.teste("Horário '25:00' é rejeitado", () ->
                lanca(ValidacaoException.class, () -> ConversorEntrada.hora("25:00")));
        r.teste("Período com data final anterior à inicial é rejeitado", () ->
                lanca(ValidacaoException.class, () -> new Periodo(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 1))));
        r.teste("Formatador de telefone: 11 dígitos -> (11) 98888-7777", () ->
                igual("(11) 98888-7777", Formatador.telefone("11988887777")));
    }

    private static void testesAcesso(MiniTestRunner r) {
        r.grupo("Controle de acesso por perfil");
        r.teste("Barbeiro não pode cadastrar cliente", () -> {
            Cenario c = new Cenario();
            lanca(AcessoNegadoException.class, () -> c.core.clientes().cadastrar(c.loginThiago, "X", "11999999999", null));
        });
        r.teste("Gerente não pode cadastrar funcionário (só o dono)", () -> {
            Cenario c = new Cenario();
            lanca(AcessoNegadoException.class, () -> c.core.funcionarios().cadastrarBarbeiro(c.gerente,
                    new DadosFuncionario("Novo", "11933333333", "33333333333", "n@vibe.com", "", "40", true)));
        });
        r.teste("Barbeiro não pode criar agendamento (só dono/gerente editam agenda)", () -> {
            Cenario c = new Cenario();
            lanca(AcessoNegadoException.class, () -> c.core.agenda().agendar(c.loginThiago,
                    c.joao.getIdCliente(), c.thiago.getIdBarbeiro(), "08/10/2026", "10:00"));
        });
        r.teste("Gerente não pode registrar atendimento (só dono/barbeiro)", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.joao, c.thiago, 11, 0);
            lanca(AcessoNegadoException.class, () -> c.core.atendimento().finalizar(c.gerente,
                    a.getIdAgendamento(), List.of(c.corte), FormaPagamento.PIX));
        });
    }

    private static void testesClientes(MiniTestRunner r) {
        r.grupo("Clientes");
        r.teste("Cadastro grava telefone só com dígitos e 0 pontos", () -> {
            Cenario c = new Cenario();
            igual("11988887777", c.joao.getTelefone());
            igual(0, c.joao.getPontosFidelidade());
            igual(null, c.maria.getEmail());
        });
        r.teste("Nome obrigatório", () -> {
            Cenario c = new Cenario();
            lanca(ValidacaoException.class, () -> c.core.clientes().cadastrar(c.gerente, "  ", "11999999999", null));
        });
        r.teste("Pesquisa por parte do nome (sem caixa) e por telefone", () -> {
            Cenario c = new Cenario();
            igual(1, c.core.clientes().pesquisar("joão").size());
            igual("Maria Souza", c.core.clientes().pesquisar("3333").get(0).getNome());
        });
        r.teste("Cliente com histórico de agenda não pode ser excluído", () -> {
            Cenario c = new Cenario();
            c.agendar(c.joao, c.thiago, 11, 0);
            lanca(RegraNegocioException.class, () -> c.core.clientes().excluir(c.gerente, c.joao.getIdCliente()));
            c.core.clientes().excluir(c.gerente, c.maria.getIdCliente());
            igual(1, c.core.clientes().listar().size());
        });
    }

    private static void testesFuncionarios(MiniTestRunner r) {
        r.grupo("Barbeiros e gerentes");
        r.teste("Barbeiro cadastrado recebe login = CPF e senha inicial padrão '123'", () -> {
            Cenario c = new Cenario();
            igual("11111111111", c.loginThiago.getLogin());
            igual("123", c.loginThiago.getSenha());
            igual(c.thiago.getIdBarbeiro(), c.loginThiago.getIdBarbeiro());
        });
        r.teste("Comissão vazia assume o padrão de 40%", () -> {
            Cenario c = new Cenario();
            Barbeiro b = c.core.funcionarios().cadastrarBarbeiro(c.dono,
                    new DadosFuncionario("Bruno Fade", "11933333333", "33333333333", "bruno@vibe.com", "", "", true));
            igual(40.0, b.getPercentComissao());
        });
        r.teste("CPF/login duplicado é rejeitado", () -> {
            Cenario c = new Cenario();
            lanca(ValidacaoException.class, () -> c.core.funcionarios().cadastrarGerente(c.dono,
                    new DadosFuncionario("Outro", "11944444444", "11111111111", "o@vibe.com", "", null, true)));
        });
        r.teste("Gerente edita nome do barbeiro, mas não altera comissão", () -> {
            Cenario c = new Cenario();
            c.core.funcionarios().atualizarBarbeiro(c.gerente, c.thiago.getIdBarbeiro(),
                    new DadosFuncionario("Thiago C.", "11911111111", null, null, null, "40", true));
            igual("Thiago C.", c.thiago.getNome());
            lanca(AcessoNegadoException.class, () -> c.core.funcionarios().atualizarBarbeiro(c.gerente,
                    c.thiago.getIdBarbeiro(), new DadosFuncionario("Thiago C.", "11911111111", null, null, null, "60", true)));
        });
        r.teste("Remover barbeiro sem histórico => EXCLUIDO (barbeiro e login apagados)", () -> {
            Cenario c = new Cenario();
            igual(ResultadoRemocaoBarbeiro.EXCLUIDO, c.core.funcionarios().removerBarbeiro(c.dono, c.rafael.getIdBarbeiro()));
            verdadeiro(c.core.repositorios().barbeiros().buscarPorId(c.rafael.getIdBarbeiro()).isEmpty(), "barbeiro deveria sumir");
            verdadeiro(c.core.repositorios().usuarios().buscarPorLogin("22222222222").isEmpty(), "login deveria sumir");
        });
        r.teste("Remover barbeiro com histórico => INATIVADO (histórico preservado, login removido)", () -> {
            Cenario c = new Cenario();
            c.agendar(c.joao, c.thiago, 11, 0);
            igual(ResultadoRemocaoBarbeiro.INATIVADO, c.core.funcionarios().removerBarbeiro(c.dono, c.thiago.getIdBarbeiro()));
            verdadeiro(!c.thiago.isAtivo(), "deveria estar inativo");
            verdadeiro(c.core.repositorios().usuarios().buscarPorLogin("11111111111").isEmpty(), "login deveria sumir");
        });
        r.teste("Dono não pode demitir a si mesmo nem 'demitir' um barbeiro como gerente", () -> {
            Cenario c = new Cenario();
            Usuario outroDono = new Usuario();
            outroDono.setPerfil(PerfilUsuario.DONO);
            outroDono.setIdUsuario(c.gerente.getIdUsuario());
            lanca(RegraNegocioException.class, () -> c.core.funcionarios().demitirGerente(outroDono, c.gerente.getIdUsuario()));
            lanca(RegraNegocioException.class, () -> c.core.funcionarios().demitirGerente(c.dono, c.loginThiago.getIdUsuario()));
            c.core.funcionarios().demitirGerente(c.dono, c.gerente.getIdUsuario());
            igual(0, c.core.funcionarios().listarGerentes().size());
        });
    }

    private static void testesAgenda(MiniTestRunner r) {
        r.grupo("Agenda");
        r.teste("Novo agendamento começa com status AGENDADO", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.core.agenda().agendar(c.gerente, c.joao.getIdCliente(), c.thiago.getIdBarbeiro(), "08/10/2026", "14:30");
            igual(StatusAgendamento.AGENDADO, a.getStatus());
            igual(1, c.core.agenda().listarDoDia(LocalDate.of(2026, 10, 8)).size());
        });
        r.teste("Barbeiro inativo não recebe agendamento", () -> {
            Cenario c = new Cenario();
            c.thiago.setAtivo(false);
            lanca(RegraNegocioException.class, () -> c.agendar(c.joao, c.thiago, 15, 0));
        });
        r.teste("Conflito: mesmo barbeiro com intervalo < 30 min é bloqueado; 30 min é permitido", () -> {
            Cenario c = new Cenario();
            c.agendar(c.joao, c.thiago, 15, 0);
            lanca(RegraNegocioException.class, () -> c.agendar(c.maria, c.thiago, 15, 15));
            c.agendar(c.maria, c.thiago, 15, 30);
            c.agendar(c.maria, c.rafael, 15, 0);
            igual(3, c.core.agenda().listarDoDia(Cenario.AGORA.toLocalDate()).size());
        });
        r.teste("Agendamento concluído não pode ser remarcado", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.joao, c.thiago, 11, 0);
            c.core.atendimento().finalizar(c.loginThiago, a.getIdAgendamento(), List.of(c.corte), FormaPagamento.PIX);
            lanca(RegraNegocioException.class, () -> c.core.agenda().remarcar(c.gerente, a.getIdAgendamento(),
                    c.thiago.getIdBarbeiro(), Cenario.AGORA.plusDays(1)));
        });
        r.teste("Barbeiro lista apenas os próprios agendamentos pendentes", () -> {
            Cenario c = new Cenario();
            c.agendar(c.joao, c.thiago, 11, 0);
            c.agendar(c.maria, c.rafael, 11, 0);
            igual(1, c.core.agenda().listarPendentesPara(c.loginThiago).size());
            igual(2, c.core.agenda().listarPendentesPara(c.dono).size());
        });
    }

    private static void testesAtendimento(MiniTestRunner r) {
        r.grupo("Atendimento (finalização)");
        r.teste("Corte R$45 + Pomada R$40 = R$85; comissão 40% = R$34; 8 pontos; status Concluído", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.joao, c.thiago, 11, 0);
            ResultadoAtendimento res = c.core.atendimento().finalizar(c.loginThiago, a.getIdAgendamento(),
                    List.of(c.corte, c.pomada), FormaPagamento.DINHEIRO);
            igual(85.0, res.movimento().getValor());
            igual(34.0, res.movimento().getComissao());
            igual(8, res.pontosGanhos());
            igual(8, c.joao.getPontosFidelidade());
            igual(LocalDate.of(2026, 10, 7), c.joao.getUltimoCorte());
            igual(StatusAgendamento.CONCLUIDO, a.getStatus());
            igual("Corte, Produto: Pomada", res.movimento().getDescricaoServicos());
            igual(1, c.core.repositorios().movimentos().listarTodos().size());
        });
        r.teste("Comissão usa o percentual do barbeiro (Rafael 50% de R$75 = R$37,50)", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.maria, c.rafael, 11, 0);
            igual(37.5, c.core.atendimento().finalizar(c.dono, a.getIdAgendamento(),
                    List.of(c.corte, c.barba), FormaPagamento.CARTAO).movimento().getComissao());
        });
        r.teste("Não é possível finalizar duas vezes o mesmo agendamento", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.joao, c.thiago, 11, 0);
            c.core.atendimento().finalizar(c.loginThiago, a.getIdAgendamento(), List.of(c.corte), FormaPagamento.PIX);
            lanca(RegraNegocioException.class, () -> c.core.atendimento().finalizar(c.loginThiago,
                    a.getIdAgendamento(), List.of(c.corte), FormaPagamento.PIX));
        });
        r.teste("Sem itens selecionados não finaliza", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.joao, c.thiago, 11, 0);
            lanca(ValidacaoException.class, () -> c.core.atendimento().finalizar(c.loginThiago,
                    a.getIdAgendamento(), List.of(), FormaPagamento.PIX));
        });
        r.teste("Barbeiro não finaliza atendimento de outro barbeiro", () -> {
            Cenario c = new Cenario();
            Agendamento a = c.agendar(c.joao, c.rafael, 11, 0);
            lanca(AcessoNegadoException.class, () -> c.core.atendimento().finalizar(c.loginThiago,
                    a.getIdAgendamento(), List.of(c.corte), FormaPagamento.PIX));
        });
    }

    private static void testesCaixa(MiniTestRunner r) {
        r.grupo("Caixa");
        r.teste("Totais por forma de pagamento e comissão no período", () -> {
            Cenario c = criarMovimentos();
            ResumoCaixa res = c.core.caixa().consultar(c.dono, new FiltroCaixa(Periodo.dia(Cenario.AGORA.toLocalDate())));
            igual(3, res.quantidade());
            igual(160.0, res.total());
            igual(45.0, res.totalDa(FormaPagamento.DINHEIRO));
            igual(75.0, res.totalDa(FormaPagamento.PIX));
            igual(40.0, res.totalDa(FormaPagamento.CARTAO));
            igual(18.0 + 37.5 + 16.0, res.totalComissao());
        });
        r.teste("Barbeiro vê somente os próprios movimentos", () -> {
            Cenario c = criarMovimentos();
            ResumoCaixa res = c.core.caixa().consultar(c.loginThiago, new FiltroCaixa(Periodo.dia(Cenario.AGORA.toLocalDate())));
            igual(2, res.quantidade());
            verdadeiro(res.movimentos().stream().allMatch(m -> m.pertenceAoBarbeiro(c.thiago.getIdBarbeiro())), "só do Thiago");
        });
        r.teste("Filtros por valor exato e busca textual", () -> {
            Cenario c = criarMovimentos();
            Periodo hoje = Periodo.dia(Cenario.AGORA.toLocalDate());
            igual(1, c.core.caixa().consultar(c.dono, new FiltroCaixa(hoje).valor(75.0)).quantidade());
            igual(1, c.core.caixa().consultar(c.dono, new FiltroCaixa(hoje).busca("pomada")).quantidade());
            igual(0, c.core.caixa().consultar(c.dono, new FiltroCaixa(Periodo.dia(LocalDate.of(2026, 1, 1)))).quantidade());
        });
    }

    private static void testesAutenticacao(MiniTestRunner r) {
        r.grupo("Autenticação e senha");
        r.teste("Login válido devolve o usuário; senha errada é recusada", () -> {
            Cenario c = new Cenario();
            igual(PerfilUsuario.BARBEIRO, c.core.autenticacao().autenticar("11111111111", "123").getPerfil());
            lanca(RegraNegocioException.class, () -> c.core.autenticacao().autenticar("11111111111", "errada"));
            lanca(ValidacaoException.class, () -> c.core.autenticacao().autenticar("", ""));
        });
        r.teste("Política de nova senha: mínimo 3, confirmação igual, diferente da atual", () -> {
            Cenario c = new Cenario();
            AutenticacaoService a = c.core.autenticacao();
            lanca(ValidacaoException.class, () -> a.alterarSenha(c.loginThiago, "123", "ab", "ab"));
            lanca(ValidacaoException.class, () -> a.alterarSenha(c.loginThiago, "123", "nova1", "nova2"));
            lanca(ValidacaoException.class, () -> a.alterarSenha(c.loginThiago, "123", "123", "123"));
            lanca(RegraNegocioException.class, () -> a.alterarSenha(c.loginThiago, "999", "nova1", "nova1"));
        });
        r.teste("Alterar senha com sucesso permite login com a nova senha", () -> {
            Cenario c = new Cenario();
            c.core.autenticacao().alterarSenha(c.loginThiago, "123", "segura9", "segura9");
            igual("Thiago Corte", c.core.autenticacao().autenticar("11111111111", "segura9").getNome());
        });
        r.teste("Recuperar senha: gera provisória de 8 caracteres (modo demonstração) e ela funciona", () -> {
            Cenario c = new Cenario();
            ResultadoRecuperacao res = c.core.autenticacao().recuperarSenha("11111111111").orElseThrow();
            verdadeiro(!res.enviada(), "modo demo não envia");
            igual(8, res.senhaParaExibir().length());
            igual("th***@vibe.com", res.emailMascarado());
            c.core.autenticacao().autenticar("11111111111", res.senhaParaExibir());
        });
        r.teste("Recuperar senha: login inexistente => vazio; sem e-mail => erro", () -> {
            Cenario c = new Cenario();
            igual(Optional.empty(), c.core.autenticacao().recuperarSenha("nao-existe"));
            c.loginRafael.setEmail(null);
            lanca(RegraNegocioException.class, () -> c.core.autenticacao().recuperarSenha("22222222222"));
        });
        r.teste("Codificador SHA-256 com salt confere a senha correta e recusa a errada", () -> {
            SenhaSha256 sha = new SenhaSha256();
            String h = sha.codificar("minhaSenha");
            verdadeiro(!h.contains("minhaSenha"), "não pode guardar em texto puro");
            verdadeiro(sha.confere("minhaSenha", h), "deveria conferir");
            verdadeiro(!sha.confere("outra", h), "não deveria conferir");
        });
    }

    private static void testesDashboardRelatorios(MiniTestRunner r) {
        r.grupo("Dashboard e relatórios");
        r.teste("Dashboard do dono: faturamento do dia R$160 e 3 agendamentos", () -> {
            Cenario c = criarMovimentos();
            List<Indicador> ind = c.core.dashboard().indicadoresPara(c.dono);
            igual(160.0, ind.get(0).valor());
            igual(3.0, ind.get(3).valor());
        });
        r.teste("Dashboard do barbeiro mostra só os números dele (comissão dia R$34)", () -> {
            Cenario c = criarMovimentos();
            List<Indicador> ind = c.core.dashboard().indicadoresPara(c.loginThiago);
            igual(85.0, ind.get(0).valor());
            igual(34.0, ind.get(2).valor());
        });
        r.teste("Relatório por barbeiro aponta o destaque (Thiago R$85 > Rafael R$75)", () -> {
            Cenario c = criarMovimentos();
            ResultadoRelatorio rel = c.core.relatorios().gerar(c.gerente, TipoRelatorio.BARBEIROS, Periodo.dia(Cenario.AGORA.toLocalDate()));
            igual("Thiago Corte", rel.getCards().get("Destaque"));
            igual("Thiago Corte", rel.getLinhas().get(0).get(0));
        });
        r.teste("Relatório de serviços: 'Corte' é o mais pedido", () -> {
            Cenario c = criarMovimentos();
            ResultadoRelatorio rel = c.core.relatorios().gerar(c.dono, TipoRelatorio.SERVICOS, Periodo.dia(Cenario.AGORA.toLocalDate()));
            igual("Corte", rel.getCards().get("Mais pedido"));
        });
        r.teste("Relatório de agenda calcula taxa de conclusão (3 de 4 = 75%)", () -> {
            Cenario c = criarMovimentos();
            c.agendar(c.maria, c.rafael, 17, 0);
            ResultadoRelatorio rel = c.core.relatorios().gerar(c.dono, TipoRelatorio.AGENDA, Periodo.dia(Cenario.AGORA.toLocalDate()));
            igual("4", rel.getCards().get("Total agendamentos"));
            igual("75%", rel.getCards().get("Taxa de conclusão"));
        });
        r.teste("Barbeiro não acessa relatórios gerenciais", () -> {
            Cenario c = new Cenario();
            lanca(AcessoNegadoException.class, () -> c.core.relatorios().gerar(c.loginThiago, TipoRelatorio.RESUMO,
                    Periodo.dia(Cenario.AGORA.toLocalDate())));
        });
    }

    /** 3 atendimentos hoje: Thiago (45 dinheiro, 40 cartão-pomada) e Rafael (75 PIX). */
    private static Cenario criarMovimentos() {
        Cenario c = new Cenario();
        Agendamento a1 = c.agendar(c.joao, c.thiago, 9, 0);
        Agendamento a2 = c.agendar(c.maria, c.rafael, 9, 0);
        Agendamento a3 = c.agendar(c.maria, c.thiago, 9, 30);
        c.core.atendimento().finalizar(c.loginThiago, a1.getIdAgendamento(), List.of(c.corte), FormaPagamento.DINHEIRO);
        c.core.atendimento().finalizar(c.loginRafael, a2.getIdAgendamento(), List.of(c.corte, c.barba), FormaPagamento.PIX);
        c.core.atendimento().finalizar(c.loginThiago, a3.getIdAgendamento(), List.of(c.pomada), FormaPagamento.CARTAO);
        return c;
    }
}
