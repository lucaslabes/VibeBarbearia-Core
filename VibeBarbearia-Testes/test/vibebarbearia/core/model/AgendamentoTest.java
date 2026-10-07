package vibebarbearia.core.model;

import org.junit.Test;

import static org.junit.Assert.*;

/** RF08/RF11 — ciclo de vida do agendamento: Agendado → Concluído. */
public class AgendamentoTest {

    @Test
    public void novoAgendamentoComecaPendenteEPodeSerConcluido() {
        Agendamento a = new Agendamento();
        assertTrue(a.isPendente());
        a.concluir();
        assertEquals(StatusAgendamento.CONCLUIDO, a.getStatus());
    }

    @Test
    public void naoPodeConcluirDuasVezes() {
        Agendamento a = new Agendamento();
        a.concluir();
        assertThrows(IllegalStateException.class, a::concluir);
    }
}
