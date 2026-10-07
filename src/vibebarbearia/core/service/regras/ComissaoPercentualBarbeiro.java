package vibebarbearia.core.service.regras;

import vibebarbearia.core.model.Barbeiro;

/**
 * Regra atual (extraída de TelaRegistroAtendimento.finalizarAtendimento):
 * comissão = valor * percentComissao / 100, arredondada em centavos.
 */
public class ComissaoPercentualBarbeiro implements PoliticaComissao {
    @Override
    public double calcular(Barbeiro barbeiro, double valor) {
        return Math.round(valor * barbeiro.getPercentComissao()) / 100.0;
    }
}
