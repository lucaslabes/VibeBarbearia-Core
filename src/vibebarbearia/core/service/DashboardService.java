package vibebarbearia.core.service;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.AgendamentoRepository;
import vibebarbearia.core.repository.MovimentoCaixaRepository;
import vibebarbearia.core.util.Periodo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Indicadores do dashboard. Substitui os ~15 métodos getFaturamentoXxx / getComissaoXxx
 * quase idênticos de BancoEmMemoria (e corrige getComissaoTotal, que devolvia
 * a comissão do dia). Usa Clock injetado em vez de LocalDate.now() (testável).
 */
public class DashboardService {

    private final MovimentoCaixaRepository movimentos;
    private final AgendamentoRepository agendamentos;
    private final Clock relogio;

    public DashboardService(MovimentoCaixaRepository movimentos, AgendamentoRepository agendamentos, Clock relogio) {
        this.movimentos = movimentos;
        this.agendamentos = agendamentos;
        this.relogio = relogio;
    }

    public List<Indicador> indicadoresPara(Usuario u) {
        LocalDate hoje = LocalDate.now(relogio);
        if (u.isBarbeiro()) {
            Integer id = u.getIdBarbeiro();
            Predicate<MovimentoCaixa> meus = m -> m.pertenceAoBarbeiro(id);
            Predicate<Agendamento> meusAg = a -> a.pertenceAoBarbeiro(id);
            return List.of(
                    new Indicador("Meu faturamento (dia)", soma(Periodo.dia(hoje), meus, MovimentoCaixa::getValor), true),
                    new Indicador("Meu faturamento (mês)", soma(Periodo.mesDe(hoje), meus, MovimentoCaixa::getValor), true),
                    new Indicador("Minhas comissões (dia)", soma(Periodo.dia(hoje), meus, MovimentoCaixa::getComissao), true),
                    new Indicador("Minhas comissões (mês)", soma(Periodo.mesDe(hoje), meus, MovimentoCaixa::getComissao), true),
                    new Indicador("Meus agendamentos (dia)", contar(hoje, meusAg), false),
                    new Indicador("Pendentes (dia)", contar(hoje, meusAg.and(Agendamento::isPendente)), false));
        }
        Predicate<MovimentoCaixa> todos = m -> true;
        return List.of(
                new Indicador("Faturamento do Dia", soma(Periodo.dia(hoje), todos, MovimentoCaixa::getValor), true),
                new Indicador("Faturamento do Mês", soma(Periodo.mesDe(hoje), todos, MovimentoCaixa::getValor), true),
                new Indicador("Faturamento do Ano", soma(Periodo.anoDe(hoje), todos, MovimentoCaixa::getValor), true),
                new Indicador("Agendamentos do Dia", contar(hoje, a -> true), false),
                new Indicador("Comissões do Dia", soma(Periodo.dia(hoje), todos, MovimentoCaixa::getComissao), true),
                new Indicador("Comissões do Mês", soma(Periodo.mesDe(hoje), todos, MovimentoCaixa::getComissao), true));
    }

    public double soma(Periodo p, Predicate<MovimentoCaixa> filtro, ToDoubleFunction<MovimentoCaixa> campo) {
        return movimentos.listarPorPeriodo(p).stream().filter(filtro).mapToDouble(campo).sum();
    }

    private double contar(LocalDate dia, Predicate<Agendamento> filtro) {
        return agendamentos.listarPorPeriodo(Periodo.dia(dia)).stream().filter(filtro).count();
    }
}
