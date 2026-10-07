package vibebarbearia.core.util;

import vibebarbearia.core.exception.ValidacaoException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Converte texto digitado em tipos de domínio. Unifica código duplicado:
 * TelaCaixa.parseValor == TelaRegistroAtendimento.parsePreco (idênticos),
 * parse de data em TelaAgenda/TelaCaixa/TelaRelatorios, parse manual de hora
 * (split(":") + faixas 0..23 / 0..59) em TelaAgenda.abrirFormularioAgendamento.
 */
public final class ConversorEntrada {
    private ConversorEntrada() {}

    /** "R$ 35,90" -> 35.90; vazio -> null; inválido -> ValidacaoException. */
    public static Double valorMonetario(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String n = raw.trim().toLowerCase(Locale.ROOT).replace("r$", "").replace(" ", "").replace(",", ".");
        try {
            return Double.parseDouble(n);
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Valor inválido. Use números (ex: 35 ou 35,90).");
        }
    }

    /** "40%" ou "40,5" -> número; usado para comissão. */
    public static double percentual(String raw) {
        if (raw == null || raw.isBlank()) throw new ValidacaoException("Comissão é obrigatória.");
        try {
            return Double.parseDouble(raw.trim().replace("%", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Comissão deve ser um número entre 0 e 100.");
        }
    }

    public static LocalDate data(String raw) {
        try {
            return LocalDate.parse(raw == null ? "" : raw.trim(), Formatador.DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data inválida. Use o formato dd/MM/aaaa.");
        }
    }

    public static LocalTime hora(String raw) {
        try {
            return LocalTime.parse(raw == null ? "" : raw.trim(), Formatador.HORA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Horário inválido. Use o formato HH:mm (ex: 10:00, 14:30).");
        }
    }
}
