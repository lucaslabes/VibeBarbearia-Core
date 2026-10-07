package vibebarbearia.core.model;

/** Status possíveis de um agendamento (mesmos valores da coluna ENUM do MySQL). */
public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CONCLUIDO("Concluído");

    private final String rotulo;

    StatusAgendamento(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }
}
