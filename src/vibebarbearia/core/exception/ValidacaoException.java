package vibebarbearia.core.exception;

/** Dado de entrada inválido (campo obrigatório, formato, faixa de valores). */
public class ValidacaoException extends RegraNegocioException {
    private static final long serialVersionUID = 1L;

    public ValidacaoException(String mensagem) { super(mensagem); }
}
