package vibebarbearia.core.validation;

import vibebarbearia.core.exception.ValidacaoException;

/**
 * Regras da nova senha (extraídas do listener do botão Salvar
 * em TelaAlterarSenha.abrir).
 */
public class PoliticaSenha {

    public void validarNovaSenha(String atual, String nova, String confirmacao) {
        if (isVazio(atual) || isVazio(nova) || isVazio(confirmacao)) {
            throw new ValidacaoException("Preencha todos os campos.");
        }
        if (nova.trim().length() < RegrasNegocio.SENHA_MIN_CARACTERES) {
            throw new ValidacaoException("A nova senha deve ter pelo menos "
                    + RegrasNegocio.SENHA_MIN_CARACTERES + " caracteres.");
        }
        if (!nova.trim().equals(confirmacao.trim())) {
            throw new ValidacaoException("A confirmação não confere com a nova senha.");
        }
        if (nova.trim().equals(atual.trim())) {
            throw new ValidacaoException("A nova senha deve ser diferente da atual.");
        }
    }

    private static boolean isVazio(String s) { return s == null || s.isBlank(); }
}
