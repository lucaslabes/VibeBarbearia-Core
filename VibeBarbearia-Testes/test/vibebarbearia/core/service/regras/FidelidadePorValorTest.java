package vibebarbearia.core.service.regras;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** RF11 — 1 ponto de fidelidade a cada R$ 10,00 pagos (só valores inteiros de pontos). */
public class FidelidadePorValorTest {

    private final PoliticaFidelidade fidelidade = new FidelidadePorValor();

    @Test
    public void atendimentoDe85ReaisGera8Pontos() {
        assertEquals(8, fidelidade.pontosPara(85.00));
    }

    @Test
    public void limiteDe10ReaisGeraExatamenteUmPonto() {
        assertEquals(0, fidelidade.pontosPara(9.99));
        assertEquals(1, fidelidade.pontosPara(10.00));
    }

    @Test
    public void valorZeroOuNegativoNaoGeraPontos() {
        assertEquals(0, fidelidade.pontosPara(0));
        assertEquals(0, fidelidade.pontosPara(-50));
    }
}
