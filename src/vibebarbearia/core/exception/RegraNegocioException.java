package vibebarbearia.core.exception;

/**
 * Violação de regra de negócio. A camada de apresentação (Swing ou Web)
 * captura esta exceção e exibe a mensagem ao usuário — o core nunca
 * chama JOptionPane/mostrarMensagem.
 */
public class RegraNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) { super(mensagem); }
}
