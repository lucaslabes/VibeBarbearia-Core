package vibebarbearia.core.service.regras;

import org.junit.Before;
import org.junit.Test;
import vibebarbearia.core.model.Barbeiro;

import static org.junit.Assert.assertEquals;

/** RF11 — comissão = valor do atendimento × percentual do barbeiro (arredondada em centavos). */
public class ComissaoPercentualBarbeiroTest {

    private static final double CENTAVO = 0.001;
    private PoliticaComissao politica;

    @Before
    public void preparar() {
        politica = new ComissaoPercentualBarbeiro();
    }

    @Test
    public void deveCalcular40PorCentoDe85Reais() {
        assertEquals(34.00, politica.calcular(new Barbeiro("Thiago", "11911111111", 40), 85.00), CENTAVO);
    }

    @Test
    public void deveUsarOPercentualDeCadaBarbeiro() {
        assertEquals(37.50, politica.calcular(new Barbeiro("Rafael", "11922222222", 50), 75.00), CENTAVO);
    }

    @Test
    public void comissaoZeroPorCentoResultaEmZero() {
        assertEquals(0.0, politica.calcular(new Barbeiro("Aprendiz", "11933333333", 0), 120.00), CENTAVO);
    }

    @Test
    public void deveArredondarParaCentavos() {
        // 33,33 × 33% = 10,9989 → R$ 11,00
        assertEquals(11.00, politica.calcular(new Barbeiro("Bruno", "11944444444", 33), 33.33), CENTAVO);
    }
}
