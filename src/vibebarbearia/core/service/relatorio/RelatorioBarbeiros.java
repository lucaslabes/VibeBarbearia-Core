package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.Barbeiro;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Formatador;

import java.util.HashMap;
import java.util.Map;

/** De TelaRelatorios.relatorioBarbeiros: faturamento, comissão, qtd e % por barbeiro. */
public class RelatorioBarbeiros implements GeradorRelatorio {
    @Override public TipoRelatorio tipo() { return TipoRelatorio.BARBEIROS; }

    @Override
    public ResultadoRelatorio gerar(DadosRelatorio d) {
        Map<String, Double> fat = Agregador.novoMapa();
        Map<String, Double> com = new HashMap<>();
        Map<String, Integer> qtd = new HashMap<>();
        for (Barbeiro b : d.barbeiros()) fat.put(b.getNome(), 0.0);
        for (MovimentoCaixa m : d.movimentos()) {
            fat.merge(m.getNomeBarbeiro(), m.getValor(), Double::sum);
            com.merge(m.getNomeBarbeiro(), m.getComissao(), Double::sum);
            qtd.merge(m.getNomeBarbeiro(), 1, Integer::sum);
        }
        double total = fat.values().stream().mapToDouble(Double::doubleValue).sum();
        ResultadoRelatorio r = new ResultadoRelatorio("Desempenho por barbeiro",
                "Barbeiro", "Atendimentos", "Faturamento", "Comissão", "% do total")
                .card("Faturamento total", Formatador.moeda(total))
                .card("Comissões totais", Formatador.moeda(com.values().stream().mapToDouble(Double::doubleValue).sum()))
                .card("Barbeiros", String.valueOf(fat.size()))
                .card("Destaque", Agregador.maior(fat));
        for (Map.Entry<String, Double> e : Agregador.ordenarDesc(fat)) {
            r.linha(e.getKey(), qtd.getOrDefault(e.getKey(), 0), Formatador.moeda(e.getValue()),
                    Formatador.moeda(com.getOrDefault(e.getKey(), 0.0)),
                    Formatador.percentual(Agregador.percentual(e.getValue(), total)));
        }
        return r;
    }
}
