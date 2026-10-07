package vibebarbearia.core.repository;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.util.Periodo;

import java.util.Comparator;
import java.util.List;

public interface AgendamentoRepository extends Repositorio<Agendamento> {

    default List<Agendamento> listarPorPeriodo(Periodo periodo) {
        return listarTodos().stream()
                .filter(a -> periodo.contem(a.getDataHorario()))
                .sorted(Comparator.comparing(Agendamento::getDataHorario))
                .toList();
    }

    default boolean existePorBarbeiro(int idBarbeiro) {
        return listarTodos().stream().anyMatch(a -> a.pertenceAoBarbeiro(idBarbeiro));
    }

    default boolean existePorCliente(int idCliente) {
        return listarTodos().stream()
                .anyMatch(a -> a.getCliente() != null && Integer.valueOf(idCliente).equals(a.getCliente().getIdCliente()));
    }
}
