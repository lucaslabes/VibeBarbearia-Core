package vibebarbearia.core.service.relatorio;

/** Tipos de relatório (antes constantes String REL_* em TelaRelatorios). */
public enum TipoRelatorio {
    RESUMO("Resumo geral"),
    BARBEIROS("Por barbeiro"),
    PAGAMENTOS("Formas de pagamento"),
    AGENDA("Agenda"),
    CLIENTES("Clientes"),
    SERVICOS("Serviços");

    private final String rotulo;

    TipoRelatorio(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }
}
