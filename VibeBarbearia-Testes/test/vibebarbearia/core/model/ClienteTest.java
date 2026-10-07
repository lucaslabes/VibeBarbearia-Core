package vibebarbearia.core.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/** RF03/RF11 — saldo de pontos de fidelidade do cliente. */
public class ClienteTest {

    private Cliente cliente;

    @Before
    public void preparar() {
        cliente = new Cliente("João Silva", "11988887777", null);
    }

    @Test
    public void adicionarPontosAcumulaESoAceitaValoresPositivos() {
        cliente.adicionarPontos(8);
        cliente.adicionarPontos(5);
        assertEquals(13, cliente.getPontosFidelidade());
        assertThrows(IllegalArgumentException.class, () -> cliente.adicionarPontos(-1));
    }

    @Test
    public void resgatarPontosDebitaDoSaldo() {
        cliente.adicionarPontos(250);
        cliente.resgatarPontos(200);
        assertEquals(50, cliente.getPontosFidelidade());
    }

    @Test
    public void naoPodeResgatarMaisPontosDoQueOSaldo() {
        cliente.adicionarPontos(50);
        assertThrows(IllegalStateException.class, () -> cliente.resgatarPontos(100));
        assertEquals(50, cliente.getPontosFidelidade());
    }
}
