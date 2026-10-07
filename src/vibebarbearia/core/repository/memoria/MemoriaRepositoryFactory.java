package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.repository.*;

/** Fábrica de repositórios em memória: usada nos testes do main() (sem MySQL). */
public class MemoriaRepositoryFactory implements RepositoryFactory {
    private final ClienteRepository clientes = new ClienteRepositoryMemoria();
    private final BarbeiroRepository barbeiros = new BarbeiroRepositoryMemoria();
    private final ServicoRepository servicos = new ServicoRepositoryMemoria();
    private final AgendamentoRepository agendamentos = new AgendamentoRepositoryMemoria();
    private final MovimentoCaixaRepository movimentos = new MovimentoCaixaRepositoryMemoria();
    private final UsuarioRepository usuarios = new UsuarioRepositoryMemoria();

    @Override public ClienteRepository clientes() { return clientes; }
    @Override public BarbeiroRepository barbeiros() { return barbeiros; }
    @Override public ServicoRepository servicos() { return servicos; }
    @Override public AgendamentoRepository agendamentos() { return agendamentos; }
    @Override public MovimentoCaixaRepository movimentos() { return movimentos; }
    @Override public UsuarioRepository usuarios() { return usuarios; }
}
