package vibebarbearia.core.service;

import org.junit.Before;
import org.junit.Test;
import vibebarbearia.core.model.*;
import vibebarbearia.core.repository.memoria.MemoriaRepositoryFactory;
import vibebarbearia.core.service.regras.ComissaoPercentualBarbeiro;
import vibebarbearia.core.service.regras.FidelidadePorValor;
import vibebarbearia.core.service.regras.PoliticaAcesso;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.*;

/**
 * RF11 — finalização do atendimento usando apenas repositórios EM MEMÓRIA
 * (sem MySQL) e relógio fixo em 07/10/2026 10:00.
 */
public class AtendimentoServiceTest {

    private static final double CENTAVO = 0.001;
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private MemoriaRepositoryFactory repos;
    private AtendimentoService servico;
    private Usuario barbeiroLogado;
    private Cliente joao;
    private Agendamento agendamento;
    private final Servico corte = new Servico("Corte", 45.00, TipoItem.SERVICO);
    private final Servico pomada = new Servico("Pomada", 40.00, TipoItem.PRODUTO);

    @Before
    public void preparar() {
        repos = new MemoriaRepositoryFactory();
        Clock relogio = Clock.fixed(LocalDateTime.of(2026, 10, 7, 10, 0).atZone(FUSO).toInstant(), FUSO);
        servico = new AtendimentoService(repos.agendamentos(), repos.clientes(), repos.movimentos(),
                new PoliticaAcesso(), new ComissaoPercentualBarbeiro(), new FidelidadePorValor(), relogio);

        Barbeiro thiago = repos.barbeiros().salvar(new Barbeiro("Thiago Corte", "11911111111", 40));
        joao = repos.clientes().salvar(new Cliente("João Silva", "11988887777", null));
        agendamento = repos.agendamentos().salvar(new Agendamento(joao, thiago, LocalDateTime.of(2026, 10, 7, 9, 0)));

        barbeiroLogado = new Usuario();
        barbeiroLogado.setPerfil(PerfilUsuario.BARBEIRO);
        barbeiroLogado.setIdBarbeiro(thiago.getIdBarbeiro());
    }

    @Test
    public void calculaTotalEDescricaoDosItens() {
        assertEquals(85.00, servico.calcularTotal(List.of(corte, pomada)), CENTAVO);
        assertEquals("Corte, Produto: Pomada", AtendimentoService.descreverItens(List.of(corte, pomada)));
    }

    @Test
    public void finalizarCalculaComissaoPontosELancaNoCaixa() {
        ResultadoAtendimento r = servico.finalizar(barbeiroLogado, agendamento.getIdAgendamento(),
                List.of(corte, pomada), FormaPagamento.DINHEIRO);

        assertEquals(85.00, r.valorPago(), CENTAVO);
        assertEquals(34.00, r.movimento().getComissao(), CENTAVO);
        assertEquals(8, r.pontosGanhos());
        assertEquals(StatusAgendamento.CONCLUIDO, agendamento.getStatus());
        assertEquals(LocalDate.of(2026, 10, 7), joao.getUltimoCorte());
        assertEquals(1, repos.movimentos().listarTodos().size());
    }

    @Test
    public void finalizarComResgateAplicaDescontoECalculaSobreOValorPago() {
        joao.adicionarPontos(250);
        ResultadoAtendimento r = servico.finalizar(barbeiroLogado, agendamento.getIdAgendamento(),
                List.of(corte, pomada), FormaPagamento.PIX, true);

        assertEquals(85.00, r.valorBruto(), CENTAVO);
        assertEquals(20.00, r.desconto(), CENTAVO);
        assertEquals(65.00, r.valorPago(), CENTAVO);
        assertEquals(26.00, r.movimento().getComissao(), CENTAVO); // 40% de 65
        assertEquals(200, r.pontosResgatados());
        assertEquals(50 + 6, joao.getPontosFidelidade());           // sobra 50 + ganha 6
    }
}
