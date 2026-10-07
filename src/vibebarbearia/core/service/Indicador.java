package vibebarbearia.core.service;

/** Card do dashboard: título + valor numérico (a UI decide como formatar). */
public record Indicador(String titulo, double valor, boolean monetario) {
}
