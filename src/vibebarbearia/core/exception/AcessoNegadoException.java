package vibebarbearia.core.exception;

/** O perfil do usuário não permite a operação. */
public class AcessoNegadoException extends RegraNegocioException {
    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) { super(mensagem); }
}
