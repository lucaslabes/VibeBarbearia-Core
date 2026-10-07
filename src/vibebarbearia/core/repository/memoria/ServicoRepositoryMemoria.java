package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.model.Servico;
import vibebarbearia.core.repository.ServicoRepository;

public class ServicoRepositoryMemoria extends RepositorioMemoria<Servico> implements ServicoRepository {
    public ServicoRepositoryMemoria() { super(Servico::getIdServico, Servico::setIdServico); }
}
