package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.FormaPagamento;
import vibebarbearia.core.service.ResumoCaixa;
import vibebarbearia.core.util.Formatador;

/** De TelaRelatorios.relatorioPagamentos (reaproveita ResumoCaixa). */
public class RelatorioPagamentos implements GeradorRelatorio {
    @Override public TipoRelatorio tipo() { return TipoRelatorio.PAGAMENTOS; }

    @Override
    public ResultadoRelatorio gerar(DadosRelatorio d) {
        ResumoCaixa resumo = ResumoCaixa.de(d.movimentos());
        ResultadoRelatorio r = new ResultadoRelatorio("Formas de pagamento", "Forma", "Qtd", "Valor", "% do total")
                .card("Total", Formatador.moeda(resumo.total()));
        for (FormaPagamento f : FormaPagamento.values()) {
            r.card(f.getRotulo(), Formatador.moeda(resumo.totalDa(f)));
            long qtd = d.movimentos().stream().filter(m -> m.getFormaPagamento() == f).count();
            if (qtd > 0) {
                r.linha(f.getRotulo(), qtd, Formatador.moeda(resumo.totalDa(f)),
                        Formatador.percentual(Agregador.percentual(resumo.totalDa(f), resumo.total())));
            }
        }
        return r;
    }
}
