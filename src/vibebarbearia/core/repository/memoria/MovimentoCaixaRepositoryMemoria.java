package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.repository.MovimentoCaixaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovimentoCaixaRepositoryMemoria implements MovimentoCaixaRepository {

    private final List<MovimentoCaixa> movimentos = new ArrayList<>();
    private int sequencia;

    @Override
    public MovimentoCaixa registrar(MovimentoCaixa m) {
        m.setIdMovimento(++sequencia);
        movimentos.add(m);
        return m;
    }

    @Override
    public Optional<MovimentoCaixa> buscarPorId(int id) {
        return movimentos.stream().filter(m -> m.getIdMovimento() == id).findFirst();
    }

    @Override
    public List<MovimentoCaixa> listarTodos() { return new ArrayList<>(movimentos); }
}
