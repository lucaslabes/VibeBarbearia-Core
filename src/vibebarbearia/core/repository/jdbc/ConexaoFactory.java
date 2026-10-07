package vibebarbearia.core.repository.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/** Abstração da origem de conexões (DIP: repositórios não conhecem URL/usuário/senha). */
@FunctionalInterface
public interface ConexaoFactory {
    Connection abrir() throws SQLException;
}
