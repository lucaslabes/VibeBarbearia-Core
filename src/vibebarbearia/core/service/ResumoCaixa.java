package vibebarbearia.core.service;

import vibebarbearia.core.model.FormaPagamento;
import vibebarbearia.core.model.MovimentoCaixa;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Resultado da consulta de caixa: lista + totais (antes calculados em TelaCaixa.carregarDados). */
public record ResumoCaixa(List<MovimentoCaixa> movimentos, double total, double totalComissao,
                          Map<FormaPagamento, Double> totalPorForma) {

    public static ResumoCaixa de(List<MovimentoCaixa> lista) {
        Map<FormaPagamento, Double> porForma = new EnumMap<>(FormaPagamento.class);
        for (FormaPagamento f : FormaPagamento.values()) porForma.put(f, 0.0);
        lista.forEach(m -> porForma.merge(m.getFormaPagamento(), m.getValor(), Double::sum));
        return new ResumoCaixa(List.copyOf(lista),
                lista.stream().mapToDouble(MovimentoCaixa::getValor).sum(),
                lista.stream().mapToDouble(MovimentoCaixa::getComissao).sum(),
                porForma);
    }

    public double totalDa(FormaPagamento f) { return totalPorForma.getOrDefault(f, 0.0); }

    public int quantidade() { return movimentos.size(); }

    /** Valor que fica com a barbearia: total recebido menos as comissões dos barbeiros. */
    public double saldoBarbearia() { return total - totalComissao; }

    /** Valor médio por atendimento (0 quando não há movimentos). */
    public double ticketMedio() { return movimentos.isEmpty() ? 0 : total / movimentos.size(); }
}
