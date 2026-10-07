package vibebarbearia.core.exception;

/** Registro inexistente no repositório. */
public class EntidadeNaoEncontradaException extends RegraNegocioException {
    private static final long serialVersionUID = 1L;

    public EntidadeNaoEncontradaException(String mensagem) { super(mensagem); }
}
