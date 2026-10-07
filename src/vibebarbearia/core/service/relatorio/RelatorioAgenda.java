package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.util.Formatador;

/** De TelaRelatorios.relatorioAgenda: total, concluídos, pendentes, taxa de conclusão. */
public class RelatorioAgenda implements GeradorRelatorio {
    @Override public TipoRelatorio tipo() { return TipoRelatorio.AGENDA; }

    @Override
    public ResultadoRelatorio gerar(DadosRelatorio d) {
        long total = d.agendamentos().size();
        long concl = d.agendamentos().stream().filter(Agendamento::isConcluido).count();
        ResultadoRelatorio r = new ResultadoRelatorio("Agendamentos do período", "Data", "Hora", "Cliente", "Barbeiro", "Status")
                .card("Total agendamentos", String.valueOf(total))
                .card("Concluídos", String.valueOf(concl))
                .card("Pendentes", String.valueOf(total - concl))
                .card("Taxa de conclusão", String.format("%.0f%%", Agregador.percentual(concl, total)));
        for (Agendamento a : d.agendamentos()) {
            r.linha(Formatador.data(a.getDataHorario().toLocalDate()), Formatador.hora(a.getDataHorario()),
                    a.getCliente() != null ? a.getCliente().getNome() : "-",
                    a.getBarbeiro() != null ? a.getBarbeiro().getNome() : "-", a.getStatus().getRotulo());
        }
        return r;
    }
}
