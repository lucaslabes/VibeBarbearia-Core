package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Formatador;

/** De TelaRelatorios.relatorioResumo. */
public class RelatorioResumo implements GeradorRelatorio {
    @Override public TipoRelatorio tipo() { return TipoRelatorio.RESUMO; }

    @Override
    public ResultadoRelatorio gerar(DadosRelatorio d) {
        double fat = d.movimentos().stream().mapToDouble(MovimentoCaixa::getValor).sum();
        double com = d.movimentos().stream().mapToDouble(MovimentoCaixa::getComissao).sum();
        long concl = d.agendamentos().stream().filter(Agendamento::isConcluido).count();
        long pend = d.agendamentos().stream().filter(Agendamento::isPendente).count();
        ResultadoRelatorio r = new ResultadoRelatorio("Movimentos do período",
                "Data", "Hora", "Cliente", "Barbeiro", "Pagamento", "Valor", "Comissão")
                .card("Faturamento", Formatador.moeda(fat))
                .card("Comissões", Formatador.moeda(com))
                .card("Atendimentos (caixa)", String.valueOf(d.movimentos().size()))
                .card("Agenda (concl. / pend.)", concl + " / " + pend);
        for (MovimentoCaixa m : d.movimentos()) {
            r.linha(Formatador.data(m.getDataHora().toLocalDate()), Formatador.hora(m.getDataHora()),
                    m.getNomeCliente(), m.getNomeBarbeiro(), m.getFormaPagamento().getRotulo(),
                    Formatador.moeda(m.getValor()), Formatador.moeda(m.getComissao()));
        }
        return r;
    }
}
