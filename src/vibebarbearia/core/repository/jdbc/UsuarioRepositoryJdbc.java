package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.model.PerfilUsuario;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.UsuarioRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static vibebarbearia.core.repository.jdbc.JdbcExecutor.getIntOuNulo;
import static vibebarbearia.core.repository.jdbc.JdbcExecutor.setIntOuNulo;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private final JdbcExecutor jdbc;

    public UsuarioRepositoryJdbc(JdbcExecutor jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return jdbc.buscarUm("SELECT * FROM usuario WHERE id_usuario = ?", ps -> ps.setInt(1, id), this::mapear);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jdbc.listar("SELECT * FROM usuario ORDER BY nome", this::mapear);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return jdbc.buscarUm("SELECT * FROM usuario WHERE login = ?", ps -> ps.setString(1, login), this::mapear);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return jdbc.buscarUm("SELECT * FROM usuario WHERE LOWER(email) = LOWER(?)",
                ps -> ps.setString(1, email.trim()), this::mapear);
    }

    @Override
    public Optional<Usuario> buscarPorIdBarbeiro(int idBarbeiro) {
        return jdbc.buscarUm("SELECT * FROM usuario WHERE id_barbeiro = ?", ps -> ps.setInt(1, idBarbeiro), this::mapear);
    }

    @Override
    public List<Usuario> listarPorPerfil(PerfilUsuario perfil) {
        return jdbc.listar("SELECT * FROM usuario WHERE perfil = ? ORDER BY nome", ps -> ps.setString(1, perfil.name()), this::mapear);
    }

    @Override
    public boolean loginExiste(String login, Integer exceptId) {
        return jdbc.buscarUm("SELECT 1 FROM usuario WHERE login = ? AND id_usuario <> ?",
                ps -> { ps.setString(1, login); ps.setInt(2, exceptId == null ? -1 : exceptId); }, rs -> true).isPresent();
    }

    @Override
    public Usuario salvar(Usuario u) {
        if (u.getIdUsuario() == null) {
            u.setIdUsuario(jdbc.inserir("INSERT INTO usuario (login, senha, nome, telefone, email, cpf, perfil, id_barbeiro) "
                    + "VALUES (?,?,?,?,?,?,?,?)", ps -> preencher(ps, u)));
        } else {
            jdbc.executar("UPDATE usuario SET login=?, senha=?, nome=?, telefone=?, email=?, cpf=?, perfil=?, id_barbeiro=? "
                    + "WHERE id_usuario=?", ps -> { preencher(ps, u); ps.setInt(9, u.getIdUsuario()); });
        }
        return u;
    }

    @Override
    public void excluir(int id) {
        jdbc.executar("DELETE FROM usuario WHERE id_usuario = ?", ps -> ps.setInt(1, id));
    }

    private void preencher(PreparedStatement ps, Usuario u) throws SQLException {
        ps.setString(1, u.getLogin());
        ps.setString(2, u.getSenha());
        ps.setString(3, u.getNome());
        ps.setString(4, u.getTelefone());
        ps.setString(5, u.getEmail());
        ps.setString(6, u.getCpf());
        ps.setString(7, u.getPerfil().name());
        setIntOuNulo(ps, 8, u.getIdBarbeiro());
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setLogin(rs.getString("login"));
        u.setSenha(rs.getString("senha"));
        u.setNome(rs.getString("nome"));
        u.setTelefone(rs.getString("telefone"));
        u.setEmail(rs.getString("email"));
        u.setCpf(rs.getString("cpf"));
        u.setPerfil(PerfilUsuario.valueOf(rs.getString("perfil")));
        u.setIdBarbeiro(getIntOuNulo(rs, "id_barbeiro"));
        return u;
    }
}
