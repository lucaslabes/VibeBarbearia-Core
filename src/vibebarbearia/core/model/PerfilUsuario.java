package vibebarbearia.core.model;

/** Níveis de hierarquia de acesso no sistema. */
public enum PerfilUsuario {
    DONO("Dono"),
    GERENTE("Gerente"),
    BARBEIRO("Barbeiro");

    private final String rotulo;

    PerfilUsuario(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }

    @Override
    public String toString() { return rotulo; }
}
