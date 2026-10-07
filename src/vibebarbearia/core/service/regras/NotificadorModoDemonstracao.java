package vibebarbearia.core.service.regras;

import vibebarbearia.core.model.Usuario;

/**
 * Equivale ao "modo demonstração" do EmailService original quando o SMTP não
 * está configurado: não envia nada; o serviço devolve a senha para exibição.
 */
public class NotificadorModoDemonstracao implements NotificadorSenha {
    @Override
    public boolean enviarSenhaProvisoria(Usuario usuario, String senhaProvisoria) { return false; }
}
