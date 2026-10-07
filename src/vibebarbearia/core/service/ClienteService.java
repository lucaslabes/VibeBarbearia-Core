package vibebarbearia.core.service;

import vibebarbearia.core.exception.EntidadeNaoEncontradaException;
import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.model.Cliente;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.AgendamentoRepository;
import vibebarbearia.core.repository.ClienteRepository;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;
import vibebarbearia.core.util.TextoUtil;
import vibebarbearia.core.validation.Validador;

import java.util.Comparator;
import java.util.List;

/**
 * Regras de clientes extraídas de TelaClientes
 * (abrirCadastroCliente, filtrarClientes, excluirCliente).
 */
public class ClienteService {

    private final ClienteRepository clientes;
    private final AgendamentoRepository agendamentos;
    private final PoliticaAcesso acesso;

    public ClienteService(ClienteRepository clientes, AgendamentoRepository agendamentos, PoliticaAcesso acesso) {
        this.clientes = clientes;
        this.agendamentos = agendamentos;
        this.acesso = acesso;
    }

    public Cliente cadastrar(Usuario solicitante, String nome, String telefone, String email) {
        acesso.exigir(solicitante, Permissao.GERENCIAR_CLIENTES);
        Cliente c = new Cliente();
        preencher(c, nome, telefone, email);
        c.setPontosFidelidade(0);
        return clientes.salvar(c);
    }

    public Cliente atualizar(Usuario solicitante, int idCliente, String nome, String telefone, String email) {
        acesso.exigir(solicitante, Permissao.GERENCIAR_CLIENTES);
        Cliente c = buscar(idCliente);
        preencher(c, nome, telefone, email);
        return clientes.salvar(c);
    }

    /** Não exclui cliente com histórico de agenda (o MySQL recusaria pela FK fk_ag_cliente). */
    public void excluir(Usuario solicitante, int idCliente) {
        acesso.exigir(solicitante, Permissao.GERENCIAR_CLIENTES);
        buscar(idCliente);
        if (agendamentos.existePorCliente(idCliente)) {
            throw new RegraNegocioException("Cliente possui agendamentos no histórico e não pode ser excluído.");
        }
        clientes.excluir(idCliente);
    }

    public Cliente buscar(int idCliente) {
        return clientes.buscarPorId(idCliente)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado."));
    }

    public List<Cliente> listar() {
        return clientes.listarTodos().stream()
                .sorted(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    /** Busca por nome (sem diferenciar maiúsculas) ou por dígitos do telefone. */
    public List<Cliente> pesquisar(String termo) {
        String t = TextoUtil.normalizarBusca(termo);
        if (t.isEmpty()) return listar();
        String digitos = TextoUtil.apenasDigitos(t);
        return listar().stream()
                .filter(c -> TextoUtil.contemIgnorandoCaixa(c.getNome(), t)
                        || (!digitos.isEmpty() && c.getTelefone() != null && c.getTelefone().contains(digitos)))
                .toList();
    }

    private void preencher(Cliente c, String nome, String telefone, String email) {
        c.setNome(Validador.obrigatorio(nome, "Nome é obrigatório."));
        c.setTelefone(Validador.telefone(telefone));
        c.setEmail(Validador.emailOpcional(email));
    }
}
