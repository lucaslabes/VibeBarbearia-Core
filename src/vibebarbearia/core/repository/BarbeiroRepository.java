package vibebarbearia.core.repository;

import vibebarbearia.core.model.Barbeiro;

import java.util.List;

public interface BarbeiroRepository extends Repositorio<Barbeiro> {
    default List<Barbeiro> listarAtivos() {
        return listarTodos().stream().filter(Barbeiro::isAtivo).toList();
    }
}
