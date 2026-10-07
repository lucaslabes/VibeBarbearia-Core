package vibebarbearia.core.service.relatorio;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Utilitários de agregação usados pelas estratégias (somar por chave, ordenar, % do total). */
final class Agregador {
    private Agregador() {}

    static <K> List<Map.Entry<K, Double>> ordenarDesc(Map<K, Double> mapa) {
        return mapa.entrySet().stream().sorted(Map.Entry.<K, Double>comparingByValue(Comparator.reverseOrder())).toList();
    }

    static double percentual(double parte, double total) { return total > 0 ? parte * 100.0 / total : 0; }

    static <K> String maior(Map<K, ? extends Number> mapa) {
        return mapa.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue().doubleValue()))
                .map(e -> String.valueOf(e.getKey())).orElse("-");
    }

    static <K> Map<K, Double> novoMapa() { return new LinkedHashMap<>(); }
}
