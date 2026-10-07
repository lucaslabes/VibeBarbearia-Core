package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.model.Cliente;
import vibebarbearia.core.repository.ClienteRepository;

public class ClienteRepositoryMemoria extends RepositorioMemoria<Cliente> implements ClienteRepository {
    public ClienteRepositoryMemoria() { super(Cliente::getIdCliente, Cliente::setIdCliente); }
}
