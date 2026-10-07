package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.model.Barbeiro;
import vibebarbearia.core.repository.BarbeiroRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BarbeiroRepositoryJdbc implements BarbeiroRepository {

    private final JdbcExecutor jdbc;

    public BarbeiroRepositoryJdbc(JdbcExecutor jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Barbeiro> listarTodos() {
        return jdbc.listar("SELECT * FROM barbeiro ORDER BY nome", this::mapear);
    }

    @Override
    public Optional<Barbeiro> buscarPorId(int id) {
        return jdbc.buscarUm("SELECT * FROM barbeiro WHERE id_barbeiro = ?", ps -> ps.setInt(1, id), this::mapear);
    }

    @Override
    public Barbeiro salvar(Barbeiro b) {
        if (b.getIdBarbeiro() == null) {
            b.setIdBarbeiro(jdbc.inserir("INSERT INTO barbeiro (nome, telefone, percent_comissao, ativo) VALUES (?,?,?,?)",
                    ps -> { ps.setString(1, b.getNome()); ps.setString(2, b.getTelefone());
                            ps.setDouble(3, b.getPercentComissao()); ps.setBoolean(4, b.isAtivo()); }));
        } else {
            jdbc.executar("UPDATE barbeiro SET nome=?, telefone=?, percent_comissao=?, ativo=? WHERE id_barbeiro=?",
                    ps -> { ps.setString(1, b.getNome()); ps.setString(2, b.getTelefone());
                            ps.setDouble(3, b.getPercentComissao()); ps.setBoolean(4, b.isAtivo());
                            ps.setInt(5, b.getIdBarbeiro()); });
        }
        return b;
    }

    @Override
    public void excluir(int id) {
        jdbc.executar("DELETE FROM barbeiro WHERE id_barbeiro = ?", ps -> ps.setInt(1, id));
    }

    private Barbeiro mapear(ResultSet rs) throws SQLException {
        Barbeiro b = new Barbeiro();
        b.setIdBarbeiro(rs.getInt("id_barbeiro"));
        b.setNome(rs.getString("nome"));
        b.setTelefone(rs.getString("telefone"));
        b.setPercentComissao(rs.getDouble("percent_comissao"));
        b.setAtivo(rs.getBoolean("ativo"));
        return b;
    }
}
