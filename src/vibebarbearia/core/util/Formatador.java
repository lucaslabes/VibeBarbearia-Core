package vibebarbearia.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Formatação centralizada. Substitui DateTimeFormatter.ofPattern("dd/MM/yyyy")
 * e String.format("R$ %.2f") repetidos dezenas de vezes nas telas, e
 * formatarTelefone duplicado em TelaClientes e TelaBarbeiros.
 */
public final class Formatador {
    private Formatador() {}

    /*
     * ResolverStyle.STRICT + "uuuu": com o padrão SMART do Java, "31/02/2026" era
     * aceito e virava 28/02/2026 (defeito encontrado pelo teste JUnit
     * ConversorEntradaTest.dataInexistenteNaoPodeSerAjustadaSilenciosamente).
     * No modo STRICT o ano precisa ser "uuuu" (ano proléptico) em vez de "yyyy".
     */
    public static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public static String moeda(double valor) {
        return String.format(PT_BR, "R$ %.2f", valor);
    }

    public static String percentual(double valor) {
        return String.format(PT_BR, "%.1f%%", valor);
    }

    public static String data(LocalDate d) { return d == null ? "" : d.format(DATA); }
    public static String hora(LocalDateTime d) { return d == null ? "" : d.format(HORA); }
    public static String dataHora(LocalDateTime d) { return d == null ? "" : d.format(DATA_HORA); }

    public static String telefone(String telefone) {
        String d = TextoUtil.apenasDigitos(telefone);
        if (d.length() == 11) return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        if (d.length() == 10) return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        return telefone == null ? "" : telefone;
    }
}
