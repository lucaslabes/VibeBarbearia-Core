package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.model.Barbeiro;
import vibebarbearia.core.model.Cliente;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Periodo;

import java.util.List;

/** Dados de entrada já filtrados pelo período, compartilhados por todas as estratégias. */
public record DadosRelatorio(Periodo periodo, List<MovimentoCaixa> movimentos, List<Agendamento> agendamentos,
                             List<Barbeiro> barbeiros, List<Cliente> clientes) {
}
