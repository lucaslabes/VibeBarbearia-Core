package vibebarbearia.core.repository;

import java.util.List;
import java.util.Optional;

/** Operações de consulta comuns (ISP: separado da escrita). */
public interface RepositorioLeitura<T> {
    Optional<T> buscarPorId(int id);
    List<T> listarTodos();
}
