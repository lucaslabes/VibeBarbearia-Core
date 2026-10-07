package vibebarbearia.core.validation;

import org.junit.Test;
import vibebarbearia.core.exception.ValidacaoException;

import static org.junit.Assert.*;

/** RF03/RF04/RF05 — validação de telefone, CPF, e-mail e comissão. */
public class ValidadorTest {

    @Test
    public void telefoneComMascaraENormalizadoParaDigitos() {
        assertEquals("11988887777", Validador.telefone("(11) 98888-7777"));
        assertEquals("1133334444", Validador.telefone("(11) 3333-4444"));
    }

    @Test
    public void telefoneComMenosDe10DigitosERejeitado() {
        assertThrows(ValidacaoException.class, () -> Validador.telefone("119888877"));
    }

    @Test
    public void cpfPrecisaDe11Digitos() {
        assertEquals("12345678901", Validador.cpf("123.456.789-01"));
        assertThrows(ValidacaoException.class, () -> Validador.cpf("1234567890"));
    }

    @Test
    public void emailValidoInvalidoEOpcional() {
        assertTrue(Validador.isEmailValido("a@b.co"));
        assertFalse(Validador.isEmailValido("lucas.com"));
        assertFalse(Validador.isEmailValido("a@b"));
        assertNull(Validador.emailOpcional(""));
    }

    @Test
    public void comissaoDeveFicarEntre0E100() {
        assertEquals(100.0, Validador.comissao(100), 0.001);
        assertThrows(ValidacaoException.class, () -> Validador.comissao(100.01));
        assertThrows(ValidacaoException.class, () -> Validador.comissao(-1));
    }
}
