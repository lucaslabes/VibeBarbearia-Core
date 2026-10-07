package vibebarbearia.core.exception;

/** Falha técnica de acesso a dados (ex.: SQLException encapsulada). */
public class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
