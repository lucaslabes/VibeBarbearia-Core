package vibebarbearia.core.service.regras;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * REGRA NOVA (Etapa 7) — resgate de pontos: 100 pontos = R$ 10,00,
 * apenas blocos inteiros e desconto máximo de 50% do atendimento.
 */
public class DescontoFidelidadeTest {

    private static final double CENTAVO = 0.001;
    private final DescontoFidelidade regra = new DescontoFidelidade();

    @Test
    public void com250PontosResgata200PontosE20Reais() {
        DescontoFidelidade.Resgate r = regra.calcular(250, 85.00);
        assertEquals(200, r.pontosUsados());
        assertEquals(20.00, r.valorDesconto(), CENTAVO);
    }

    @Test
    public void menosDe100PontosNaoPermiteResgate() {
        assertEquals(DescontoFidelidade.Resgate.NENHUM, regra.calcular(99, 85.00));
    }

    @Test
    public void descontoNaoPassaDe50PorCentoDoAtendimento() {
        // R$ 45,00 → limite R$ 22,50 → só 2 blocos (R$ 20,00), mesmo com 1000 pontos
        DescontoFidelidade.Resgate r = regra.calcular(1000, 45.00);
        assertEquals(200, r.pontosUsados());
        assertEquals(20.00, r.valorDesconto(), CENTAVO);
    }

    @Test
    public void atendimentoBaratoDemaisNaoPermiteNenhumBloco() {
        // R$ 15,00 → limite R$ 7,50 < R$ 10,00 de um bloco
        assertEquals(DescontoFidelidade.Resgate.NENHUM, regra.calcular(500, 15.00));
    }

    @Test
    public void exatamente100PontosEm100ReaisDaUmBloco() {
        DescontoFidelidade.Resgate r = regra.calcular(100, 100.00);
        assertEquals(100, r.pontosUsados());
        assertEquals(10.00, r.valorDesconto(), CENTAVO);
    }
}
