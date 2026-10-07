package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.model.Barbeiro;
import vibebarbearia.core.repository.BarbeiroRepository;

public class BarbeiroRepositoryMemoria extends RepositorioMemoria<Barbeiro> implements BarbeiroRepository {
    public BarbeiroRepositoryMemoria() { super(Barbeiro::getIdBarbeiro, Barbeiro::setIdBarbeiro); }
}
