package vibebarbearia.core.service.regras;

import vibebarbearia.core.validation.RegrasNegocio;

/**
 * REGRA NOVA (Etapa 7): resgate de pontos de fidelidade como desconto.
 *
 * - Cada bloco de 100 pontos vale R$ 10,00 de desconto.
 * - Só blocos inteiros são resgatados (sobram os pontos que não fecham um bloco).
 * - O desconto não pode passar de 50% do valor do atendimento.
 *
 * Exemplo: cliente com 250 pontos e atendimento de R$ 85,00 → resgata 200 pontos
 * = R$ 20,00 de desconto (limite seria R$ 42,50), paga R$ 65,00 e fica com 50 pontos.
 */
public class DescontoFidelidade {

    /** Resultado do cálculo: quantos pontos serão usados e quanto vale o desconto. */
    public record Resgate(int pontosUsados, double valorDesconto) {
        public static final Resgate NENHUM = new Resgate(0, 0.0);
    }

    private final int pontosPorResgate;
    private final double valorPorResgate;
    private final double descontoMaximoPercent;

    public DescontoFidelidade() {
        this(RegrasNegocio.PONTOS_POR_RESGATE, RegrasNegocio.VALOR_POR_RESGATE, RegrasNegocio.DESCONTO_MAXIMO_PERCENT);
    }

    public DescontoFidelidade(int pontosPorResgate, double valorPorResgate, double descontoMaximoPercent) {
        this.pontosPorResgate = pontosPorResgate;
        this.valorPorResgate = valorPorResgate;
        this.descontoMaximoPercent = descontoMaximoPercent;
    }

    /** Cálculo puro do resgate para um saldo de pontos e um valor de atendimento. */
    public Resgate calcular(int pontosDisponiveis, double valorAtendimento) {
        if (pontosDisponiveis <= 0 || valorAtendimento <= 0) return Resgate.NENHUM;
        int blocosDisponiveis = pontosDisponiveis / pontosPorResgate;
        double limite = valorAtendimento * descontoMaximoPercent / 100.0;
        int blocosPeloLimite = (int) Math.floor(limite / valorPorResgate);
        int blocos = Math.min(blocosDisponiveis, blocosPeloLimite);
        if (blocos == 0) return Resgate.NENHUM;
        return new Resgate(blocos * pontosPorResgate, blocos * valorPorResgate);
    }
}
