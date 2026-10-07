package vibebarbearia.core.repository;

/**
 * Abstract Factory: cria a família de repositórios de uma mesma tecnologia
 * (memória para testes, JDBC/MySQL para produção). Os serviços não sabem
 * qual implementação recebem (DIP).
 */
public interface RepositoryFactory {
    ClienteRepository clientes();
    BarbeiroRepository barbeiros();
    ServicoRepository servicos();
    AgendamentoRepository agendamentos();
    MovimentoCaixaRepository movimentos();
    UsuarioRepository usuarios();
}
