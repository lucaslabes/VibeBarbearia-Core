package vibebarbearia.core.util;

import vibebarbearia.core.exception.ValidacaoException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Intervalo fechado de datas. Substitui o filtro
 * "!d.isBefore(inicio) && !d.isAfter(fim)" repetido em BancoEmMemoria
 * (movimentosPeriodo, getFaturamentoPeriodo, getComissaoPeriodo),
 * TelaCaixa.carregarDados e TelaRelatorios (movimentosNoPeriodo, agendamentosNoPeriodo).
 */
public record Periodo(LocalDate inicio, LocalDate fim) {

    public Periodo {
        if (inicio == null || fim == null) throw new ValidacaoException("Informe data inicial e final.");
        if (fim.isBefore(inicio)) {
            throw new ValidacaoException("A data final não pode ser anterior à data inicial.");
        }
    }

    public static Periodo dia(LocalDate d) { return new Periodo(d, d); }

    public static Periodo mesDe(LocalDate d) {
        return new Periodo(d.withDayOfMonth(1), d.withDayOfMonth(d.lengthOfMonth()));
    }

    public static Periodo anoDe(LocalDate d) {
        return new Periodo(d.withDayOfYear(1), d.withDayOfYear(d.lengthOfYear()));
    }

    /** Número de dias do período, contando início e fim (ex.: 01 a 07 = 7 dias). */
    public long quantidadeDias() {
        return java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1;
    }

    public boolean contem(LocalDate d) {
        return d != null && !d.isBefore(inicio) && !d.isAfter(fim);
    }

    public boolean contem(LocalDateTime dt) {
        return dt != null && contem(dt.toLocalDate());
    }
}
