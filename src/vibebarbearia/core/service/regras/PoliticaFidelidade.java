package vibebarbearia.core.service.regras;

/** Strategy: quantos pontos de fidelidade um atendimento gera. */
@FunctionalInterface
public interface PoliticaFidelidade {
    int pontosPara(double valorAtendimento);
}
