package vibebarbearia.core.service.regras;

import vibebarbearia.core.model.Usuario;

/**
 * Porta de saída para enviar a senha provisória (e-mail, SMS, log...).
 * O desktop pode implementar com o EmailService/JavaMail; a web com outro provedor.
 * Assim o core não depende de javax.mail (DIP).
 */
public interface NotificadorSenha {

    /** @return true se a senha foi efetivamente entregue ao usuário. */
    boolean enviarSenhaProvisoria(Usuario usuario, String senhaProvisoria);
}
