package vibebarbearia.core.service.regras;

import java.util.Objects;

/**
 * Compatível com o banco atual do desktop (coluna usuario.senha em texto puro).
 * Use apenas enquanto a base não for migrada para SenhaSha256.
 */
public class SenhaTextoPlano implements CodificadorSenha {
    @Override public String codificar(String senhaPura) { return senhaPura; }
    @Override public boolean confere(String senhaPura, String armazenada) { return Objects.equals(senhaPura, armazenada); }
}
