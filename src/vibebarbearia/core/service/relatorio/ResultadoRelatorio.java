package vibebarbearia.core.service.relatorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Relatório independente de interface: cards (título -> valor) + tabela.
 * A tela Swing transforma em JTable; a web em HTML/JSON.
 */
public class ResultadoRelatorio {
    private final String titulo;
    private final Map<String, String> cards = new LinkedHashMap<>();
    private final List<String> colunas;
    private final List<List<Object>> linhas = new ArrayList<>();

    public ResultadoRelatorio(String titulo, String... colunas) {
        this.titulo = titulo;
        this.colunas = List.of(colunas);
    }

    public ResultadoRelatorio card(String nome, String valor) { cards.put(nome, valor); return this; }
    public ResultadoRelatorio linha(Object... valores) { linhas.add(List.of(valores)); return this; }

    public String getTitulo() { return titulo; }
    public Map<String, String> getCards() { return cards; }
    public List<String> getColunas() { return colunas; }
    public List<List<Object>> getLinhas() { return linhas; }
}
