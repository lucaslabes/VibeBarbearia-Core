package vibebarbearia.core.util;

import java.util.Locale;

/**
 * Funções de texto puras. apenasDigitos estava duplicado em TelaClientes e
 * TelaBarbeiros; limparNome/iniciais estavam em SessaoUsuario (classe de sessão).
 */
public final class TextoUtil {
    private TextoUtil() {}

    public static String apenasDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static String normalizarBusca(String termo) {
        return termo == null ? "" : termo.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean contemIgnorandoCaixa(String texto, String termoNormalizado) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(termoNormalizado);
    }

    /** Remove sufixos de cargo como " (Gerente)" ou " - Dono" e normaliza espaços. */
    public static String limparNome(String bruto) {
        if (bruto == null) return "";
        String n = bruto.trim();
        n = n.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
        n = n.replaceAll("(?i)\\s*[-–·|]\\s*(dono|gerente|barbeiro|administrador)\\s*$", "").trim();
        return n.replaceAll("\\s+", " ");
    }

    public static String iniciais(String nome) {
        String n = limparNome(nome);
        if (n.isEmpty()) return "?";
        String[] p = n.split(" ");
        String ini = p.length == 1 ? p[0].substring(0, 1) : p[0].substring(0, 1) + p[p.length - 1].substring(0, 1);
        return ini.toUpperCase(Locale.ROOT);
    }

    /** Mascara e-mail: "lucas@x.com" -> "lu***@x.com" (antes em EmailService). */
    public static String mascararEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] p = email.trim().split("@", 2);
        String user = p[0].isEmpty() ? "*" : p[0];
        return (user.length() <= 2 ? user.substring(0, 1) : user.substring(0, 2)) + "***@" + p[1];
    }
}
