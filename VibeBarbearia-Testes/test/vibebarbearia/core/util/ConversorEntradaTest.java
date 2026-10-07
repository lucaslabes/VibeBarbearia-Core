package vibebarbearia.core.util;

import org.junit.Test;
import vibebarbearia.core.exception.ValidacaoException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

/** RF07/RF08/RF12 — conversão do texto digitado nas telas para valores. */
public class ConversorEntradaTest {

    @Test
    public void valorMonetarioAceitaFormatoBrasileiro() {
        assertEquals(35.90, ConversorEntrada.valorMonetario("R$ 35,90"), 0.001);
        assertEquals(45.0, ConversorEntrada.valorMonetario("45"), 0.001);
        assertNull("campo vazio = sem filtro", ConversorEntrada.valorMonetario("  "));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.valorMonetario("abc"));
    }

    @Test
    public void percentualAceitaSimboloEVirgula() {
        assertEquals(37.5, ConversorEntrada.percentual("37,5%"), 0.001);
    }

    @Test
    public void horaValidaEInvalida() {
        assertEquals(LocalTime.of(14, 30), ConversorEntrada.hora("14:30"));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.hora("25:00"));
    }

    @Test
    public void dataValidaEhConvertida() {
        assertEquals(LocalDate.of(2026, 10, 7), ConversorEntrada.data("07/10/2026"));
        assertEquals("ano bissexto", LocalDate.of(2028, 2, 29), ConversorEntrada.data("29/02/2028"));
    }

    @Test
    public void dataInexistenteNaoPodeSerAjustadaSilenciosamente() {
        // 31/02 não existe: deve ser rejeitada, e não virar 28/02 (agendamento no dia errado)
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.data("31/02/2026"));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.data("29/02/2026"));
    }
}
