package vibebarbearia.core.service;

import vibebarbearia.core.model.MovimentoCaixa;

/** Dados que a tela precisa para exibir o resumo do atendimento finalizado. */
public record ResultadoAtendimento(MovimentoCaixa movimento, int pontosGanhos, int totalPontosCliente) {
}
