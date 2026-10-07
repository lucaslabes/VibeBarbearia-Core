package vibebarbearia.core.service.regras;

import vibebarbearia.core.validation.RegrasNegocio;

import java.security.SecureRandom;

/** Mesma regra de EmailService.gerarSenhaProvisoria (8 chars, sem 0/O/1/I), agora fora do serviço de e-mail. */
public class GeradorSenhaAleatoria implements GeradorSenha {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String gerar() {
        String alfabeto = RegrasNegocio.SENHA_PROVISORIA_ALFABETO;
        StringBuilder sb = new StringBuilder(RegrasNegocio.SENHA_PROVISORIA_TAMANHO);
        for (int i = 0; i < RegrasNegocio.SENHA_PROVISORIA_TAMANHO; i++) {
            sb.append(alfabeto.charAt(random.nextInt(alfabeto.length())));
        }
        return sb.toString();
    }
}
