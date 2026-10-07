package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.model.StatusAgendamento;
import vibebarbearia.core.repository.AgendamentoRepository;
import vibebarbearia.core.repository.BarbeiroRepository;
import vibebarbearia.core.repository.ClienteRepository;
import vibebarbearia.core.util.Periodo;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Antes: AgendamentoDAO criava "new ClienteDAO()" e "new BarbeiroDAO()" dentro de si
 * (acoplamento a classes concretas). Agora recebe as interfaces por construtor (DIP).
 */
public class AgendamentoRepositoryJdbc implements AgendamentoRepository {

    private final JdbcExecutor jdbc;
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;

    public AgendamentoRepositoryJdbc(JdbcExecutor jdbc, ClienteRepository clientes, BarbeiroRepository barbeiros) {
        this.jdbc = jdbc;
        this.clientes = clientes;
        this.barbeiros = barbeiros;
    }

    @Override
    public List<Agendamento> listarTodos() {
        return jdbc.listar("SELECT * FROM agendamento ORDER BY data_horario", this::mapear);
    }

    /** Filtra no SQL em vez de carregar a tabela inteira (desempenho). */
    @Override
    public List<Agendamento> listarPorPeriodo(Periodo p) {
        return jdbc.listar("SELECT * FROM agendamento WHERE DATE(data_horario) BETWEEN ? AND ? ORDER BY data_horario",
                ps -> { ps.setDate(1, Date.valueOf(p.inicio())); ps.setDate(2, Date.valueOf(p.fim())); }, this::mapear);
    }

    @Override
    public boolean existePorBarbeiro(int idBarbeiro) {
        return jdbc.buscarUm("SELECT 1 FROM agendamento WHERE id_barbeiro = ? LIMIT 1",
                ps -> ps.setInt(1, idBarbeiro), rs -> true).isPresent();
    }

    @Override
    public Optional<Agendamento> buscarPorId(int id) {
        return jdbc.buscarUm("SELECT * FROM agendamento WHERE id_agendamento = ?", ps -> ps.setInt(1, id), this::mapear);
    }

    @Override
    public Agendamento salvar(Agendamento a) {
        if (a.getIdAgendamento() == null) {
            a.setIdAgendamento(jdbc.inserir(
                    "INSERT INTO agendamento (id_cliente, id_barbeiro, data_horario, status) VALUES (?,?,?,?)",
                    ps -> { ps.setInt(1, a.getCliente().getIdCliente()); ps.setInt(2, a.getBarbeiro().getIdBarbeiro());
                            ps.setTimestamp(3, Timestamp.valueOf(a.getDataHorario())); ps.setString(4, a.getStatus().name()); }));
        } else {
            jdbc.executar("UPDATE agendamento SET id_cliente=?, id_barbeiro=?, data_horario=?, status=? WHERE id_agendamento=?",
                    ps -> { ps.setInt(1, a.getCliente().getIdCliente()); ps.setInt(2, a.getBarbeiro().getIdBarbeiro());
                            ps.setTimestamp(3, Timestamp.valueOf(a.getDataHorario())); ps.setString(4, a.getStatus().name());
                            ps.setInt(5, a.getIdAgendamento()); });
        }
        return a;
    }

    @Override
    public void excluir(int id) {
        jdbc.executar("DELETE FROM agendamento WHERE id_agendamento = ?", ps -> ps.setInt(1, id));
    }

    private Agendamento mapear(ResultSet rs) throws SQLException {
        Agendamento a = new Agendamento();
        a.setIdAgendamento(rs.getInt("id_agendamento"));
        a.setCliente(clientes.buscarPorId(rs.getInt("id_cliente")).orElse(null));
        a.setBarbeiro(barbeiros.buscarPorId(rs.getInt("id_barbeiro")).orElse(null));
        Timestamp ts = rs.getTimestamp("data_horario");
        if (ts != null) a.setDataHorario(ts.toLocalDateTime());
        a.setStatus(StatusAgendamento.valueOf(rs.getString("status")));
        return a;
    }
}
