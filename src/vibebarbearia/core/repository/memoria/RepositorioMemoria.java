package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.repository.Repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base genérica dos repositórios em memória (Template Method: as subclasses
 * só informam como ler/gravar o id). Evita repetir a mesma lógica 6 vezes.
 */
public abstract class RepositorioMemoria<T> implements Repositorio<T> {

    private final Map<Integer, T> dados = new LinkedHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger();
    private final Function<T, Integer> leitorId;
    private final BiConsumer<T, Integer> gravadorId;

    protected RepositorioMemoria(Function<T, Integer> leitorId, BiConsumer<T, Integer> gravadorId) {
        this.leitorId = leitorId;
        this.gravadorId = gravadorId;
    }

    @Override
    public T salvar(T entidade) {
        Integer id = leitorId.apply(entidade);
        if (id == null) {
            id = sequencia.incrementAndGet();
            gravadorId.accept(entidade, id);
        }
        dados.put(id, entidade);
        return entidade;
    }

    @Override
    public void excluir(int id) { dados.remove(id); }

    @Override
    public Optional<T> buscarPorId(int id) { return Optional.ofNullable(dados.get(id)); }

    @Override
    public List<T> listarTodos() { return new ArrayList<>(dados.values()); }
}
