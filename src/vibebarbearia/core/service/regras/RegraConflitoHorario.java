package vibebarbearia.core.service.regras;

import vibebarbearia.core.model.Agendamento;
import vibebarbearia.core.validation.RegrasNegocio;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Regra de conflito de horário da agenda, extraída do método privado
 * AgendamentoService.verificarConflito para poder ser testada isoladamente
 * (Etapa 7 — testes unitários).
 *
 * Dois atendimentos do MESMO barbeiro conflitam quando a diferença entre os
 * horários é menor que a duração de um atendimento (padrão: 30 minutos).
 */
public class RegraConflitoHorario {

    private final Duration duracaoAtendimento;

    public RegraConflitoHorario() {
        this(Duration.ofMinutes(RegrasNegocio.DURACAO_ATENDIMENTO_MINUTOS));
    }

    public RegraConflitoHorario(Duration duracaoAtendimento) {
        if (duracaoAtendimento == null || duracaoAtendimento.isNegative() || duracaoAtendimento.isZero()) {
            throw new IllegalArgumentException("Duração do atendimento deve ser positiva");
        }
        this.duracaoAtendimento = duracaoAtendimento;
    }

    /** Cálculo puro: os dois horários estão a menos de uma duração de atendimento? */
    public boolean horariosConflitam(LocalDateTime existente, LocalDateTime novo) {
        return Math.abs(Duration.between(existente, novo).toMinutes()) < duracaoAtendimento.toMinutes();
    }

    /**
     * O agendamento existente bloqueia o novo horário para o barbeiro informado?
     * Só bloqueiam agendamentos pendentes, do mesmo barbeiro e diferentes do que
     * está sendo remarcado (ignorarId).
     */
    public boolean bloqueia(Agendamento existente, Integer idBarbeiro, LocalDateTime novo, Integer ignorarId) {
        if (existente == null || existente.getDataHorario() == null) return false;
        if (ignorarId != null && ignorarId.equals(existente.getIdAgendamento())) return false;
        return existente.isPendente()
                && existente.pertenceAoBarbeiro(idBarbeiro)
                && horariosConflitam(existente.getDataHorario(), novo);
    }
}
