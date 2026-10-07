package vibebarbearia.core.service.regras;

import vibebarbearia.core.model.Barbeiro;

/** Strategy: como calcular a comissão do barbeiro sobre um atendimento. */
@FunctionalInterface
public interface PoliticaComissao {
    double calcular(Barbeiro barbeiro, double valorAtendimento);
}
