package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.Cliente;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Formatador;

import java.util.HashMap;
import java.util.Map;

/** De TelaRelatorios.relatorioClientes: gasto, visitas, ticket médio, pontos. */
public class RelatorioClientes implements GeradorRelatorio {
    @Override public TipoRelatorio tipo() { return TipoRelatorio.CLIENTES; }

    @Override
    public ResultadoRelatorio gerar(DadosRelatorio d) {
        Map<String, Double> gasto = Agregador.novoMapa();
        Map<String, Integer> visitas = new HashMap<>();
        Map<String, Integer> pontos = new HashMap<>();
        for (Cliente c : d.clientes()) pontos.put(c.getNome(), c.getPontosFidelidade());
        for (MovimentoCaixa m : d.movimentos()) {
            gasto.merge(m.getNomeCliente(), m.getValor(), Double::sum);
            visitas.merge(m.getNomeCliente(), 1, Integer::sum);
        }
        double soma = gasto.values().stream().mapToDouble(Double::doubleValue).sum();
        double ticket = d.movimentos().isEmpty() ? 0 : soma / d.movimentos().size();
        ResultadoRelatorio r = new ResultadoRelatorio("Clientes", "Cliente", "Visitas", "Total gasto", "Pontos")
                .card("Clientes no relatório", String.valueOf(gasto.size()))
                .card("Atendimentos", String.valueOf(d.movimentos().size()))
                .card("Ticket médio", Formatador.moeda(ticket))
                .card("Maior gasto", Agregador.maior(gasto));
        for (Map.Entry<String, Double> e : Agregador.ordenarDesc(gasto)) {
            r.linha(e.getKey(), visitas.getOrDefault(e.getKey(), 0), Formatador.moeda(e.getValue()),
                    pontos.getOrDefault(e.getKey(), 0));
        }
        return r;
    }
}
