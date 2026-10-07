package vibebarbearia.core.model;

/** Tipo de item do catálogo: serviço ou produto. */
public enum TipoItem {
    SERVICO("Serviço"),
    PRODUTO("Produto");

    private final String rotulo;

    TipoItem(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }

    @Override
    public String toString() { return rotulo; }
}
