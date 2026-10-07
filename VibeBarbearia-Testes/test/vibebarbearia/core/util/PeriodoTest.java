package vibebarbearia.core.util;

import org.junit.Test;
import vibebarbearia.core.exception.ValidacaoException;

import java.time.LocalDate;

import static org.junit.Assert.*;

/** RF12/RF13 — período de consulta do caixa e dos relatórios. */
public class PeriodoTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate DIA_7 = LocalDate.of(2026, 10, 7);

    @Test
    public void dataFinalAntesDaInicialERejeitada() {
        assertThrows(ValidacaoException.class, () -> new Periodo(DIA_7, DIA_1));
    }

    @Test
    public void contemIncluiOsDoisExtremos() {
        Periodo p = new Periodo(DIA_1, DIA_7);
        assertTrue(p.contem(DIA_1));
        assertTrue(p.contem(DIA_7));
        assertFalse(p.contem(DIA_7.plusDays(1)));
        assertFalse(p.contem(DIA_1.minusDays(1)));
    }

    @Test
    public void quantidadeDeDiasContaInicioEFim() {
        assertEquals(7, new Periodo(DIA_1, DIA_7).quantidadeDias());
        assertEquals(1, Periodo.dia(DIA_7).quantidadeDias());
    }

    @Test
    public void mesDeFevereiroEmAnoBissextoTem29Dias() {
        assertEquals(29, Periodo.mesDe(LocalDate.of(2028, 2, 10)).quantidadeDias());
        assertEquals(365, Periodo.anoDe(DIA_7).quantidadeDias());
    }
}
