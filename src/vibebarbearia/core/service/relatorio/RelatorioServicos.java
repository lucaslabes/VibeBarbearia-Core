package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Formatador;

import java.util.LinkedHashMap;
import java.util.Map;

/** De TelaRelatorios.relatorioServicos: contagem por item com rateio do valor. */
public class RelatorioServicos implements GeradorRelatorio {
    private static final String SEM_DESCRICAO = "(sem descrição)";

    @Override public TipoRelatorio tipo() { return TipoRelatorio.SERVICOS; }

    @Override
    public ResultadoRelatorio gerar(DadosRelatorio d) {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        Map<String, Double> valor = new LinkedHashMap<>();
        for (MovimentoCaixa m : d.movimentos()) {
            String desc = m.getDescricaoServicos();
            String[] partes = desc == null || desc.isBlank() ? new String[]{SEM_DESCRICAO} : desc.split(",");
            double rateio = m.getValor() / partes.length;
            for (String p : partes) {
                String nome = p.trim();
                if (nome.isEmpty()) continue;
                contagem.merge(nome, 1, Integer::sum);
                valor.merge(nome, rateio, Double::sum);
            }
        }
        int totalItens = contagem.values().stream().mapToInt(Integer::intValue).sum();
        ResultadoRelatorio r = new ResultadoRelatorio("Serviços realizados", "Serviço", "Qtd", "Valor (aprox.)", "% dos itens")
                .card("Tipos de serviço", String.valueOf(contagem.size()))
                .card("Itens realizados", String.valueOf(totalItens))
                .card("Atendimentos", String.valueOf(d.movimentos().size()))
                .card("Mais pedido", Agregador.maior(contagem));
        contagem.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> r.linha(e.getKey(), e.getValue(), Formatador.moeda(valor.get(e.getKey())),
                        Formatador.percentual(Agregador.percentual(e.getValue(), totalItens))));
        return r;
    }
}
