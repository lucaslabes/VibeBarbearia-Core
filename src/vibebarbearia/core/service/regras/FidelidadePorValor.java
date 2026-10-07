package vibebarbearia.core.service.regras;

import vibebarbearia.core.validation.RegrasNegocio;

/**
 * Regra atual: 1 ponto a cada R$ 10 (antes "(int) (valorTotal / 10)" com o 10 mágico
 * em TelaRegistroAtendimento.finalizarAtendimento).
 */
public class FidelidadePorValor implements PoliticaFidelidade {

    private final double valorPorPonto;

    public FidelidadePorValor() { this(RegrasNegocio.VALOR_POR_PONTO_FIDELIDADE); }

    public FidelidadePorValor(double valorPorPonto) { this.valorPorPonto = valorPorPonto; }

    @Override
    public int pontosPara(double valor) {
        return valor <= 0 ? 0 : (int) Math.floor(valor / valorPorPonto);
    }
}
