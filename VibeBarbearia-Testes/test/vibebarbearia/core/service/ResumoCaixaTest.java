package vibebarbearia.core.service;

import org.junit.Test;
import vibebarbearia.core.model.FormaPagamento;
import vibebarbearia.core.model.MovimentoCaixa;

import java.util.List;

import static org.junit.Assert.assertEquals;

/** RF12 — totais do caixa por forma de pagamento, comissões e saldo da barbearia. */
public class ResumoCaixaTest {

    private static final double CENTAVO = 0.001;

    private static MovimentoCaixa mov(double valor, double comissao, FormaPagamento forma) {
        MovimentoCaixa m = new MovimentoCaixa();
        m.setValor(valor);
        m.setComissao(comissao);
        m.setFormaPagamento(forma);
        return m;
    }

    private final ResumoCaixa resumo = ResumoCaixa.de(List.of(
            mov(45.00, 18.00, FormaPagamento.DINHEIRO),
            mov(75.00, 37.50, FormaPagamento.PIX),
            mov(40.00, 16.00, FormaPagamento.CARTAO)));

    @Test
    public void somaTotalEPorFormaDePagamento() {
        assertEquals(160.00, resumo.total(), CENTAVO);
        assertEquals(45.00, resumo.totalDa(FormaPagamento.DINHEIRO), CENTAVO);
        assertEquals(75.00, resumo.totalDa(FormaPagamento.PIX), CENTAVO);
        assertEquals(40.00, resumo.totalDa(FormaPagamento.CARTAO), CENTAVO);
    }

    @Test
    public void saldoDaBarbeariaDescontaAsComissoes() {
        assertEquals(71.50, resumo.totalComissao(), CENTAVO);
        assertEquals(88.50, resumo.saldoBarbearia(), CENTAVO);
        assertEquals(53.33, resumo.ticketMedio(), 0.01);
    }

    @Test
    public void caixaVazioTemTodosOsTotaisZerados() {
        ResumoCaixa vazio = ResumoCaixa.de(List.of());
        assertEquals(0, vazio.quantidade());
        assertEquals(0.0, vazio.total(), CENTAVO);
        assertEquals(0.0, vazio.ticketMedio(), CENTAVO);
        assertEquals(0.0, vazio.totalDa(FormaPagamento.PIX), CENTAVO);
    }
}
