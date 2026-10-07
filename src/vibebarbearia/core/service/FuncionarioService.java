package vibebarbearia.core.service;

import vibebarbearia.core.exception.EntidadeNaoEncontradaException;
import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.exception.ValidacaoException;
import vibebarbearia.core.model.*;
import vibebarbearia.core.repository.AgendamentoRepository;
import vibebarbearia.core.repository.BarbeiroRepository;
import vibebarbearia.core.repository.MovimentoCaixaRepository;
import vibebarbearia.core.repository.UsuarioRepository;
import vibebarbearia.core.service.regras.CodificadorSenha;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;
import vibebarbearia.core.util.ConversorEntrada;
import vibebarbearia.core.validation.RegrasNegocio;
import vibebarbearia.core.validation.Validador;

import java.util.List;

/**
 * Gestão de barbeiros e gerentes. Extraído do "God method"
 * TelaBarbeiros.abrirFormularioFuncionario (~415 linhas misturando layout,
 * validação, permissão e persistência) e de TelaBarbeiros.excluirFuncionario.
 */
public class FuncionarioService {

    private final BarbeiroRepository barbeiros;
    private final UsuarioRepository usuarios;
    private final AgendamentoRepository agendamentos;
    private final MovimentoCaixaRepository movimentos;
    private final PoliticaAcesso acesso;
    private final CodificadorSenha codificador;

    public FuncionarioService(BarbeiroRepository barbeiros, UsuarioRepository usuarios,
                              AgendamentoRepository agendamentos, MovimentoCaixaRepository movimentos,
                              PoliticaAcesso acesso, CodificadorSenha codificador) {
        this.barbeiros = barbeiros;
        this.usuarios = usuarios;
        this.agendamentos = agendamentos;
        this.movimentos = movimentos;
        this.acesso = acesso;
        this.codificador = codificador;
    }

    /** Cria o barbeiro e o login dele (login = CPF). Apenas o dono. */
    public Barbeiro cadastrarBarbeiro(Usuario solicitante, DadosFuncionario d) {
        acesso.exigir(solicitante, Permissao.CADASTRAR_FUNCIONARIO);
        Usuario conta = novaConta(d, PerfilUsuario.BARBEIRO);
        Barbeiro b = new Barbeiro(Validador.obrigatorio(d.nome(), "Nome é obrigatório."),
                Validador.telefone(d.telefone()), comissaoOuPadrao(d.comissao()));
        b.setAtivo(d.ativo());
        barbeiros.salvar(b);
        conta.setIdBarbeiro(b.getIdBarbeiro());
        usuarios.salvar(conta);
        return b;
    }

    /** Cria um gerente (somente login, sem ficha de barbeiro). Apenas o dono. */
    public Usuario cadastrarGerente(Usuario solicitante, DadosFuncionario d) {
        acesso.exigir(solicitante, Permissao.CADASTRAR_FUNCIONARIO);
        return usuarios.salvar(novaConta(d, PerfilUsuario.GERENTE));
    }

    /**
     * Atualiza dados do barbeiro. Gerente pode editar nome/telefone/status,
     * mas comissão só o dono altera (regra de TelaBarbeiros: "podeComissao").
     */
    public Barbeiro atualizarBarbeiro(Usuario solicitante, int idBarbeiro, DadosFuncionario d) {
        acesso.exigir(solicitante, Permissao.VISUALIZAR_FUNCIONARIOS);
        Barbeiro b = buscarBarbeiro(idBarbeiro);
        b.setNome(Validador.obrigatorio(d.nome(), "Nome é obrigatório."));
        b.setTelefone(Validador.telefone(d.telefone()));
        b.setAtivo(d.ativo());
        if (d.comissao() != null && !d.comissao().isBlank()) {
            double nova = Validador.comissao(ConversorEntrada.percentual(d.comissao()));
            if (Double.compare(nova, b.getPercentComissao()) != 0) {
                acesso.exigir(solicitante, Permissao.ALTERAR_COMISSAO);
                b.setPercentComissao(nova);
            }
        }
        barbeiros.salvar(b);
        usuarios.buscarPorIdBarbeiro(idBarbeiro).ifPresent(conta -> {
            conta.setNome(b.getNome());
            conta.setTelefone(b.getTelefone());
            usuarios.salvar(conta);
        });
        return b;
    }

    /**
     * Remove o acesso do barbeiro. Sem histórico: exclui; com histórico em
     * agenda/caixa: apenas inativa (preserva relatórios). Apenas o dono.
     * No desktop isso dependia de capturar a SQLException da FK em
     * BarbeiroDAO.excluirComUsuario; aqui a regra é explícita.
     */
    public ResultadoRemocaoBarbeiro removerBarbeiro(Usuario solicitante, int idBarbeiro) {
        acesso.exigir(solicitante, Permissao.CADASTRAR_FUNCIONARIO);
        Barbeiro b = buscarBarbeiro(idBarbeiro);
        usuarios.buscarPorIdBarbeiro(idBarbeiro).ifPresent(u -> usuarios.excluir(u.getIdUsuario()));
        if (agendamentos.existePorBarbeiro(idBarbeiro) || movimentos.existePorBarbeiro(idBarbeiro)) {
            b.setAtivo(false);
            barbeiros.salvar(b);
            return ResultadoRemocaoBarbeiro.INATIVADO;
        }
        barbeiros.excluir(idBarbeiro);
        return ResultadoRemocaoBarbeiro.EXCLUIDO;
    }

    /** Demite gerente (remove login). Apenas o dono e nunca a si mesmo. */
    public void demitirGerente(Usuario solicitante, int idUsuarioGerente) {
        acesso.exigir(solicitante, Permissao.GERENCIAR_GERENTES);
        Usuario g = usuarios.buscarPorId(idUsuarioGerente)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Gerente não encontrado."));
        if (g.getPerfil() != PerfilUsuario.GERENTE) {
            throw new RegraNegocioException("Só é possível demitir usuários com perfil Gerente por esta ação.");
        }
        if (g.getIdUsuario().equals(solicitante.getIdUsuario())) {
            throw new RegraNegocioException("Você não pode demitir o próprio usuário logado.");
        }
        usuarios.excluir(idUsuarioGerente);
    }

    public List<Barbeiro> listarBarbeiros() { return barbeiros.listarTodos(); }

    public List<Usuario> listarGerentes() { return usuarios.listarPorPerfil(PerfilUsuario.GERENTE); }

    public Barbeiro buscarBarbeiro(int id) {
        return barbeiros.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Barbeiro não encontrado."));
    }

    private Usuario novaConta(DadosFuncionario d, PerfilUsuario perfil) {
        String nome = Validador.obrigatorio(d.nome(), "Nome é obrigatório.");
        String telefone = Validador.telefone(d.telefone());
        String cpf = Validador.cpf(d.cpf());
        String email = Validador.emailObrigatorio(d.email());
        if (usuarios.loginExiste(cpf, null)) {
            throw new ValidacaoException("Este CPF/login já está em uso. Verifique o CPF informado.");
        }
        String senha = d.senha() == null || d.senha().isBlank() ? RegrasNegocio.SENHA_INICIAL_PADRAO : d.senha().trim();
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setTelefone(telefone);
        u.setCpf(cpf);
        u.setLogin(cpf);
        u.setEmail(email);
        u.setSenha(codificador.codificar(senha));
        u.setPerfil(perfil);
        return u;
    }

    private static double comissaoOuPadrao(String texto) {
        if (texto == null || texto.isBlank()) return RegrasNegocio.COMISSAO_PADRAO_PERCENT;
        return Validador.comissao(ConversorEntrada.percentual(texto));
    }
}
