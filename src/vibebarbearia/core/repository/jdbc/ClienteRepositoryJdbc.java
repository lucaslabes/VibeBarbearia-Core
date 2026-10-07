package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.model.Cliente;
import vibebarbearia.core.repository.ClienteRepository;
import vibebarbearia.core.util.Formatador;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class ClienteRepositoryJdbc implements ClienteRepository {

    private final JdbcExecutor jdbc;

    public ClienteRepositoryJdbc(JdbcExecutor jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Cliente> listarTodos() {
        return jdbc.listar("SELECT * FROM cliente ORDER BY nome", this::mapear);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return jdbc.buscarUm("SELECT * FROM cliente WHERE id_cliente = ?", ps -> ps.setInt(1, id), this::mapear);
    }

    @Override
    public Cliente salvar(Cliente c) {
        if (c.getIdCliente() == null) {
            c.setIdCliente(jdbc.inserir("INSERT INTO cliente (nome, telefone, email, data_nascimento, "
                    + "pontos_fidelidade, ultimo_corte) VALUES (?,?,?,?,?,?)", ps -> preencher(ps, c)));
        } else {
            jdbc.executar("UPDATE cliente SET nome=?, telefone=?, email=?, data_nascimento=?, "
                    + "pontos_fidelidade=?, ultimo_corte=? WHERE id_cliente=?", ps -> {
                preencher(ps, c);
                ps.setInt(7, c.getIdCliente());
            });
        }
        return c;
    }

    @Override
    public void excluir(int id) {
        jdbc.executar("DELETE FROM cliente WHERE id_cliente = ?", ps -> ps.setInt(1, id));
    }

    private void preencher(java.sql.PreparedStatement ps, Cliente c) throws SQLException {
        ps.setString(1, c.getNome());
        ps.setString(2, c.getTelefone());
        ps.setString(3, c.getEmail());
        ps.setDate(4, c.getDataNascimento() != null ? Date.valueOf(c.getDataNascimento()) : null);
        ps.setInt(5, c.getPontosFidelidade());
        ps.setString(6, c.getUltimoCorte() != null ? Formatador.data(c.getUltimoCorte()) : "-");
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNome(rs.getString("nome"));
        c.setTelefone(rs.getString("telefone"));
        c.setEmail(rs.getString("email"));
        Date nasc = rs.getDate("data_nascimento");
        if (nasc != null) c.setDataNascimento(nasc.toLocalDate());
        c.setPontosFidelidade(rs.getInt("pontos_fidelidade"));
        c.setUltimoCorte(lerData(rs.getString("ultimo_corte")));
        return c;
    }

    /** A coluna ultimo_corte é VARCHAR no banco legado ("15/06/2026" ou "-"). */
    private static LocalDate lerData(String texto) {
        try {
            return texto == null || texto.isBlank() || "-".equals(texto) ? null : LocalDate.parse(texto, Formatador.DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
