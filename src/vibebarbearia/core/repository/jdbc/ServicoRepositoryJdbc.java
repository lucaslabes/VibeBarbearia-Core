package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.model.Servico;
import vibebarbearia.core.model.TipoItem;
import vibebarbearia.core.repository.ServicoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ServicoRepositoryJdbc implements ServicoRepository {

    private final JdbcExecutor jdbc;

    public ServicoRepositoryJdbc(JdbcExecutor jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Servico> listarTodos() {
        return jdbc.listar("SELECT * FROM servico ORDER BY tipo, nome", this::mapear);
    }

    @Override
    public Optional<Servico> buscarPorId(int id) {
        return jdbc.buscarUm("SELECT * FROM servico WHERE id_servico = ?", ps -> ps.setInt(1, id), this::mapear);
    }

    @Override
    public Servico salvar(Servico s) {
        if (s.getIdServico() == null) {
            s.setIdServico(jdbc.inserir("INSERT INTO servico (nome, preco, tipo) VALUES (?,?,?)",
                    ps -> { ps.setString(1, s.getNome()); ps.setDouble(2, s.getPreco()); ps.setString(3, s.getTipo().name()); }));
        } else {
            jdbc.executar("UPDATE servico SET nome=?, preco=?, tipo=? WHERE id_servico=?",
                    ps -> { ps.setString(1, s.getNome()); ps.setDouble(2, s.getPreco());
                            ps.setString(3, s.getTipo().name()); ps.setInt(4, s.getIdServico()); });
        }
        return s;
    }

    @Override
    public void excluir(int id) {
        jdbc.executar("DELETE FROM servico WHERE id_servico = ?", ps -> ps.setInt(1, id));
    }

    private Servico mapear(ResultSet rs) throws SQLException {
        Servico s = new Servico(rs.getString("nome"), rs.getDouble("preco"), TipoItem.valueOf(rs.getString("tipo")));
        s.setIdServico(rs.getInt("id_servico"));
        return s;
    }
}
