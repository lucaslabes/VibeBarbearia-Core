package vibebarbearia.core.service;

import vibebarbearia.core.exception.AcessoNegadoException;
import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.exception.ValidacaoException;
import vibebarbearia.core.model.*;
import vibebarbearia.core.repository.AgendamentoRepository;
import vibebarbearia.core.repository.ClienteRepository;
import vibebarbearia.core.repository.MovimentoCaixaRepository;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;
import vibebarbearia.core.service.regras.PoliticaComissao;
import vibebarbearia.core.service.regras.PoliticaFidelidade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Finalização de atendimento. Extraído de
 * TelaRegistroAtendimento.finalizarAtendimento (~105 linhas) e recalcularTotal,
 * que liam JCheckBox/JRadioButton, calculavam total, comissão e pontos,
 * atualizavam agenda/cliente e gravavam o caixa — tudo dentro da tela.
 */
public class AtendimentoService {

    private final AgendamentoRepository agendamentos;
    private final ClienteRepository clientes;
    private final MovimentoCaixaRepository movimentos;
    private final PoliticaAcesso acesso;
    private final PoliticaComissao comissao;
    private final PoliticaFidelidade fidelidade;
    private final Clock relogio;

    public AtendimentoService(AgendamentoRepository agendamentos, ClienteRepository clientes,
                              MovimentoCaixaRepository movimentos, PoliticaAcesso acesso,
                              PoliticaComissao comissao, PoliticaFidelidade fidelidade, Clock relogio) {
        this.agendamentos = agendamentos;
        this.clientes = clientes;
        this.movimentos = movimentos;
        this.acesso = acesso;
        this.comissao = comissao;
        this.fidelidade = fidelidade;
        this.relogio = relogio;
    }

    /** Soma o preço dos itens selecionados (antes: recalcularTotal na tela). */
    public double calcularTotal(List<Servico> itens) {
        return itens == null ? 0 : itens.stream().mapToDouble(Servico::getPreco).sum();
    }

    public ResultadoAtendimento finalizar(Usuario solicitante, int idAgendamento,
                                         List<Servico> itens, FormaPagamento forma) {
        acesso.exigir(solicitante, Permissao.REGISTRAR_ATENDIMENTO);
        Agendamento ag = agendamentos.buscarPorId(idAgendamento)
                .orElseThrow(() -> new ValidacaoException("Selecione um agendamento pendente."));
        if (ag.isConcluido()) throw new RegraNegocioException("Este agendamento já está concluído.");
        if (solicitante.isBarbeiro() && !ag.pertenceAoBarbeiro(solicitante.getIdBarbeiro())) {
            throw new AcessoNegadoException("Você só pode finalizar atendimentos atribuídos a você.");
        }
        double total = calcularTotal(itens);
        if (total <= 0) throw new ValidacaoException("Selecione pelo menos um serviço ou produto.");
        if (forma == null) throw new ValidacaoException("Selecione a forma de pagamento.");

        LocalDateTime agora = LocalDateTime.now(relogio);
        ag.concluir();
        agendamentos.salvar(ag);

        Cliente cliente = ag.getCliente();
        int pontos = fidelidade.pontosPara(total);
        cliente.adicionarPontos(pontos);
        cliente.setUltimoCorte(agora.toLocalDate());
        clientes.salvar(cliente);

        MovimentoCaixa m = new MovimentoCaixa();
        m.setDataHora(agora);
        m.setCliente(cliente);
        m.setBarbeiro(ag.getBarbeiro());
        m.setValor(total);
        m.setComissao(comissao.calcular(ag.getBarbeiro(), total));
        m.setFormaPagamento(forma);
        m.setDescricaoServicos(itens.stream().map(Servico::getDescricaoCaixa).collect(Collectors.joining(", ")));
        m.setIdAgendamento(ag.getIdAgendamento());
        movimentos.registrar(m);

        return new ResultadoAtendimento(m, pontos, cliente.getPontosFidelidade());
    }
}
