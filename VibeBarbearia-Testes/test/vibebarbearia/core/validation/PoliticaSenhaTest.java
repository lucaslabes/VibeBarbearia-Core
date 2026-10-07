package vibebarbearia.core.validation;

import org.junit.Test;
import vibebarbearia.core.exception.ValidacaoException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/** RF14 — regras para a nova senha. */
public class PoliticaSenhaTest {

    private final PoliticaSenha politica = new PoliticaSenha();

    @Test
    public void novaSenhaComMenosDe3CaracteresERejeitada() {
        String msg = assertThrows(ValidacaoException.class,
                () -> politica.validarNovaSenha("123", "ab", "ab")).getMessage();
        assertEquals("A nova senha deve ter pelo menos 3 caracteres.", msg);
    }

    @Test
    public void confirmacaoDiferenteOuSenhaIgualAAtualSaoRejeitadas() {
        assertThrows(ValidacaoException.class, () -> politica.validarNovaSenha("123", "nova1", "nova2"));
        assertThrows(ValidacaoException.class, () -> politica.validarNovaSenha("123", "123", "123"));
        assertThrows(ValidacaoException.class, () -> politica.validarNovaSenha("", "nova1", "nova1"));
    }

    @Test
    public void senhaValidaNaoLancaExcecao() {
        politica.validarNovaSenha("123", "segura9", "segura9");
    }
}
