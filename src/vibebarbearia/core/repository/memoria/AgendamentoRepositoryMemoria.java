package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.repository.AgendamentoRepository;

public class AgendamentoRepositoryMemoria extends RepositorioMemoria<Agendamento> implements AgendamentoRepository {
    public AgendamentoRepositoryMemoria() { super(Agendamento::getIdAgendamento, Agendamento::setIdAgendamento); }
}
