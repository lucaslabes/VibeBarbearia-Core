package vibebarbearia.core.service;

/**
 * Resultado da recuperação de senha (antes EmailService.ResultadoEnvio misturava
 * envio de e-mail com resultado de negócio).
 *
 * @param senhaParaExibir preenchida só no modo demonstração (sem envio real)
 */
public record ResultadoRecuperacao(boolean enviada, String emailMascarado, String senhaParaExibir) {
}
