package vibebarbearia.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/** Agendamento de um cliente com um barbeiro em data/horário. */
public class Agendamento {
    private Integer idAgendamento;
    private Cliente cliente;
    private Barbeiro barbeiro;
    private LocalDateTime dataHorario;
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    public Agendamento() {}

    public Agendamento(Cliente cliente, Barbeiro barbeiro, LocalDateTime dataHorario) {
        this.cliente = cliente;
        this.barbeiro = barbeiro;
        this.dataHorario = dataHorario;
    }

    public Integer getIdAgendamento() { return idAgendamento; }
    public void setIdAgendamento(Integer idAgendamento) { this.idAgendamento = idAgendamento; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Barbeiro getBarbeiro() { return barbeiro; }
    public void setBarbeiro(Barbeiro barbeiro) { this.barbeiro = barbeiro; }
    public LocalDateTime getDataHorario() { return dataHorario; }
    public void setDataHorario(LocalDateTime dataHorario) { this.dataHorario = dataHorario; }
    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public boolean isConcluido() { return status == StatusAgendamento.CONCLUIDO; }
    public boolean isPendente() { return status == StatusAgendamento.AGENDADO; }

    public boolean isNoDia(LocalDate dia) {
        return dataHorario != null && dataHorario.toLocalDate().equals(dia);
    }

    public boolean pertenceAoBarbeiro(Integer idBarbeiro) {
        return idBarbeiro != null && barbeiro != null && idBarbeiro.equals(barbeiro.getIdBarbeiro());
    }

    /** Transição de estado: só um agendamento pendente pode ser concluído. */
    public void concluir() {
        if (isConcluido()) throw new IllegalStateException("Agendamento já concluído");
        this.status = StatusAgendamento.CONCLUIDO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agendamento other)) return false;
        return idAgendamento != null && idAgendamento.equals(other.idAgendamento);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idAgendamento); }
}
