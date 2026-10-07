package vibebarbearia.core.repository;

import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Periodo;

import java.util.Comparator;
import java.util.List;

/**
 * Movimentos de caixa são apenas registrados e consultados — não há
 * atualizar/excluir (ISP: não herda Repositorio, só RepositorioLeitura).
 */
public interface MovimentoCaixaRepository extends RepositorioLeitura<MovimentoCaixa> {

    MovimentoCaixa registrar(MovimentoCaixa movimento);

    default List<MovimentoCaixa> listarPorPeriodo(Periodo periodo) {
        return listarTodos().stream()
                .filter(m -> periodo.contem(m.getDataHora()))
                .sorted(Comparator.comparing(MovimentoCaixa::getDataHora))
                .toList();
    }

    default boolean existePorBarbeiro(int idBarbeiro) {
        return listarTodos().stream().anyMatch(m -> m.pertenceAoBarbeiro(idBarbeiro));
    }
}
