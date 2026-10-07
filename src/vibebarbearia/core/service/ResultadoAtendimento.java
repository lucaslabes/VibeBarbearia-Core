package vibebarbearia.core.service;

import vibebarbearia.core.model.MovimentoCaixa;

/**
 * Dados que a tela precisa para exibir o resumo do atendimento finalizado.
 *
 * @param valorBruto       soma dos itens antes do desconto
 * @param desconto         desconto por resgate de pontos (0 se não houve)
 * @param pontosResgatados pontos debitados no resgate (0 se não houve)
 */
public record ResultadoAtendimento(MovimentoCaixa movimento, int pontosGanhos, int totalPontosCliente,
                                   double valorBruto, double desconto, int pontosResgatados) {

    /** Valor efetivamente pago (o mesmo lançado no caixa). */
    public double valorPago() { return movimento.getValor(); }
}
