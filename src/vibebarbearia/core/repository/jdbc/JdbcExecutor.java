package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.exception.PersistenciaException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Elimina o boilerplate repetido em TODOS os DAOs do desktop
 * (try-with-resources + PreparedStatement + catch SQLException ->
 * RuntimeException("Erro ao ...")). Cada repositório JDBC passa a ter só
 * o SQL e o mapeamento.
 */
public class JdbcExecutor {

    @FunctionalInterface
    public interface Parametros { void aplicar(PreparedStatement ps) throws SQLException; }

    @FunctionalInterface
    public interface Mapeador<T> { T mapear(ResultSet rs) throws SQLException; }

    private final ConexaoFactory conexoes;

    public JdbcExecutor(ConexaoFactory conexoes) { this.conexoes = conexoes; }

    public <T> List<T> listar(String sql, Parametros params, Mapeador<T> mapeador) {
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            params.aplicar(ps);
            List<T> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapeador.mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao consultar: " + e.getMessage(), e);
        }
    }

    public <T> List<T> listar(String sql, Mapeador<T> mapeador) {
        return listar(sql, ps -> { }, mapeador);
    }

    public <T> Optional<T> buscarUm(String sql, Parametros params, Mapeador<T> mapeador) {
        return listar(sql, params, mapeador).stream().findFirst();
    }

    public int executar(String sql, Parametros params) {
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            params.aplicar(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao gravar: " + e.getMessage(), e);
        }
    }

    /** INSERT que devolve a chave gerada (AUTO_INCREMENT). */
    public int inserir(String sql, Parametros params) {
        try (Connection c = conexoes.abrir();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            params.aplicar(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new PersistenciaException("Chave gerada não retornada", null);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir: " + e.getMessage(), e);
        }
    }

    public static void setIntOuNulo(PreparedStatement ps, int idx, Integer valor) throws SQLException {
        if (valor != null) ps.setInt(idx, valor); else ps.setNull(idx, Types.INTEGER);
    }

    public static Integer getIntOuNulo(ResultSet rs, String coluna) throws SQLException {
        int v = rs.getInt(coluna);
        return rs.wasNull() ? null : v;
    }
}
