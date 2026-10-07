package vibebarbearia.core.service;

import vibebarbearia.core.exception.EntidadeNaoEncontradaException;
import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.model.Barbeiro;
import vibebarbearia.core.model.Cliente;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.AgendamentoRepository;
import vibebarbearia.core.repository.BarbeiroRepository;
import vibebarbearia.core.repository.ClienteRepository;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;
import vibebarbearia.core.util.ConversorEntrada;
import vibebarbearia.core.util.Periodo;
import vibebarbearia.core.validation.RegrasNegocio;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de agenda extraídas do listener do botão OK em
 * TelaAgenda.abrirFormularioAgendamento (~175 linhas) e de carregarAgendamentosDoDia.
 */
public class AgendamentoService {

    private final AgendamentoRepository agendamentos;
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;
    private final PoliticaAcesso acesso;

    public AgendamentoService(AgendamentoRepository agendamentos, ClienteRepository clientes,
                              BarbeiroRepository barbeiros, PoliticaAcesso acesso) {
        this.agendamentos = agendamentos;
        this.clientes = clientes;
        this.barbeiros = barbeiros;
        this.acesso = acesso;
    }

    /** Variante que recebe o texto digitado (dd/MM/aaaa e HH:mm), como na tela. */
    public Agendamento agendar(Usuario solicitante, int idCliente, int idBarbeiro, String data, String hora) {
        return agendar(solicitante, idCliente, idBarbeiro,
                ConversorEntrada.data(data).atTime(ConversorEntrada.hora(hora)));
    }

    /** Novo agendamento: sempre começa como AGENDADO (Concluído só no atendimento). */
    public Agendamento agendar(Usuario solicitante, int idCliente, int idBarbeiro, LocalDateTime dataHora) {
        acesso.exigir(solicitante, Permissao.EDITAR_AGENDA);
        Cliente cliente = clientes.buscarPorId(idCliente)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado."));
        Barbeiro barbeiro = barbeiroAtivo(idBarbeiro);
        verificarConflito(barbeiro, dataHora, null);
        return agendamentos.salvar(new Agendamento(cliente, barbeiro, dataHora));
    }

    /** Remarca data/horário e/ou troca o barbeiro. Status não é alterado manualmente. */
    public Agendamento remarcar(Usuario solicitante, int idAgendamento, int idBarbeiro, LocalDateTime novaDataHora) {
        acesso.exigir(solicitante, Permissao.EDITAR_AGENDA);
        Agendamento a = buscar(idAgendamento);
        if (a.isConcluido()) throw new RegraNegocioException("Agendamento concluído não pode ser remarcado.");
        Barbeiro barbeiro = barbeiroAtivo(idBarbeiro);
        verificarConflito(barbeiro, novaDataHora, a.getIdAgendamento());
        a.setBarbeiro(barbeiro);
        a.setDataHorario(novaDataHora);
        return agendamentos.salvar(a);
    }

    public List<Agendamento> listarDoDia(LocalDate dia) {
        return agendamentos.listarPorPeriodo(Periodo.dia(dia));
    }

    /** Pendentes que o usuário pode finalizar (barbeiro vê só os dele). */
    public List<Agendamento> listarPendentesPara(Usuario usuario) {
        return agendamentos.listarTodos().stream()
                .filter(Agendamento::isPendente)
                .filter(a -> !usuario.isBarbeiro() || a.pertenceAoBarbeiro(usuario.getIdBarbeiro()))
                .toList();
    }

    public Agendamento buscar(int id) {
        return agendamentos.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Agendamento não encontrado."));
    }

    private Barbeiro barbeiroAtivo(int idBarbeiro) {
        Barbeiro b = barbeiros.buscarPorId(idBarbeiro)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Barbeiro não encontrado."));
        if (!b.isAtivo()) throw new RegraNegocioException("Barbeiro inativo não pode receber agendamentos.");
        return b;
    }

    /** Regra nova: impede dois atendimentos do mesmo barbeiro em menos de 30 minutos. */
    private void verificarConflito(Barbeiro barbeiro, LocalDateTime dataHora, Integer ignorarId) {
        long janela = RegrasNegocio.DURACAO_ATENDIMENTO_MINUTOS;
        boolean conflito = listarDoDia(dataHora.toLocalDate()).stream()
                .filter(a -> ignorarId == null || !ignorarId.equals(a.getIdAgendamento()))
                .filter(Agendamento::isPendente)
                .filter(a -> a.pertenceAoBarbeiro(barbeiro.getIdBarbeiro()))
                .anyMatch(a -> Math.abs(Duration.between(a.getDataHorario(), dataHora).toMinutes()) < janela);
        if (conflito) {
            throw new RegraNegocioException("O barbeiro " + barbeiro.getNome() + " já tem atendimento próximo a este horário.");
        }
    }
}
