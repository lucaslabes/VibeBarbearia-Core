package vibebarbearia.core.service.regras;

import org.junit.Before;
import org.junit.Test;
import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.model.Barbeiro;
import vibebarbearia.core.model.Cliente;

import java.time.LocalDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** RF08/RF09 — mesmo barbeiro não pode ter atendimentos com menos de 30 minutos de diferença. */
public class RegraConflitoHorarioTest {

    private static final LocalDateTime QUINZE_HORAS = LocalDateTime.of(2026, 10, 8, 15, 0);
    private RegraConflitoHorario regra;
    private Agendamento existente;

    @Before
    public void preparar() {
        regra = new RegraConflitoHorario();
        Barbeiro thiago = new Barbeiro("Thiago", "11911111111", 40);
        thiago.setIdBarbeiro(1);
        existente = new Agendamento(new Cliente("João", "11988887777", null), thiago, QUINZE_HORAS);
        existente.setIdAgendamento(10);
    }

    @Test
    public void quinzeMinutosDeDiferencaConflita() {
        assertTrue(regra.horariosConflitam(QUINZE_HORAS, QUINZE_HORAS.plusMinutes(15)));
        assertTrue(regra.horariosConflitam(QUINZE_HORAS, QUINZE_HORAS.minusMinutes(15)));
    }

    @Test
    public void trintaMinutosDeDiferencaNaoConflita() {
        assertFalse(regra.horariosConflitam(QUINZE_HORAS, QUINZE_HORAS.plusMinutes(30)));
    }

    @Test
    public void agendamentoDeOutroBarbeiroOuConcluidoNaoBloqueia() {
        assertTrue(regra.bloqueia(existente, 1, QUINZE_HORAS.plusMinutes(10), null));
        assertFalse("outro barbeiro", regra.bloqueia(existente, 2, QUINZE_HORAS.plusMinutes(10), null));
        existente.concluir();
        assertFalse("concluído", regra.bloqueia(existente, 1, QUINZE_HORAS.plusMinutes(10), null));
    }

    @Test
    public void aoRemarcarOProprioAgendamentoNaoConflitaConsigoMesmo() {
        assertFalse(regra.bloqueia(existente, 1, QUINZE_HORAS.plusMinutes(5), 10));
    }
}
