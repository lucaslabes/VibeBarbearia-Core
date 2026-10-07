package vibebarbearia.core.repository.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexão MySQL configurável. No desktop, Conexao.java tinha host, usuário
 * e SENHA fixos no código-fonte (code smell + risco de segurança, a senha foi
 * para o GitHub). Aqui a configuração vem, em ordem de prioridade, de:
 * variáveis de ambiente VIBE_DB_URL / VIBE_DB_USUARIO / VIBE_DB_SENHA,
 * ou do arquivo config/banco.properties (fora do Git, ver banco.properties.exemplo).
 */
public class ConexaoMySQL implements ConexaoFactory {

    public static final String URL_PADRAO = "jdbc:mysql://localhost:3306/vibebarbearia"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&characterEncoding=utf8";

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoMySQL(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    /** Lê a configuração do ambiente ou de config/banco.properties. */
    public static ConexaoMySQL daConfiguracao() {
        Properties p = new Properties();
        Path arquivo = Path.of("config", "banco.properties");
        if (Files.exists(arquivo)) {
            try (InputStream in = Files.newInputStream(arquivo)) {
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possível ler " + arquivo, e);
            }
        }
        return new ConexaoMySQL(
                valor("VIBE_DB_URL", p.getProperty("db.url"), URL_PADRAO),
                valor("VIBE_DB_USUARIO", p.getProperty("db.usuario"), "root"),
                valor("VIBE_DB_SENHA", p.getProperty("db.senha"), ""));
    }

    private static String valor(String env, String doArquivo, String padrao) {
        String v = System.getenv(env);
        if (v != null && !v.isBlank()) return v;
        return doArquivo != null ? doArquivo : padrao;
    }

    @Override
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
