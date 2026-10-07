package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.repository.*;

/** Fábrica de repositórios MySQL (substitui a fachada BancoEmMemoria do desktop). */
public class JdbcRepositoryFactory implements RepositoryFactory {
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;
    private final ServicoRepository servicos;
    private final AgendamentoRepository agendamentos;
    private final MovimentoCaixaRepository movimentos;
    private final UsuarioRepository usuarios;

    public JdbcRepositoryFactory(ConexaoFactory conexoes) {
        JdbcExecutor jdbc = new JdbcExecutor(conexoes);
        clientes = new ClienteRepositoryJdbc(jdbc);
        barbeiros = new BarbeiroRepositoryJdbc(jdbc);
        servicos = new ServicoRepositoryJdbc(jdbc);
        agendamentos = new AgendamentoRepositoryJdbc(jdbc, clientes, barbeiros);
        movimentos = new MovimentoCaixaRepositoryJdbc(jdbc, clientes, barbeiros);
        usuarios = new UsuarioRepositoryJdbc(jdbc);
    }

    @Override public ClienteRepository clientes() { return clientes; }
    @Override public BarbeiroRepository barbeiros() { return barbeiros; }
    @Override public ServicoRepository servicos() { return servicos; }
    @Override public AgendamentoRepository agendamentos() { return agendamentos; }
    @Override public MovimentoCaixaRepository movimentos() { return movimentos; }
    @Override public UsuarioRepository usuarios() { return usuarios; }
}
