package vibebarbearia.core.model;

import java.util.Objects;

/** Barbeiro (funcionário que realiza atendimentos e recebe comissão). */
public class Barbeiro {
    private Integer idBarbeiro;
    private String nome;
    private String telefone;
    private double percentComissao;
    private boolean ativo = true;

    public Barbeiro() {}

    public Barbeiro(String nome, String telefone, double percentComissao) {
        this.nome = nome;
        this.telefone = telefone;
        this.percentComissao = percentComissao;
    }

    public Integer getIdBarbeiro() { return idBarbeiro; }
    public void setIdBarbeiro(Integer idBarbeiro) { this.idBarbeiro = idBarbeiro; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public double getPercentComissao() { return percentComissao; }
    public void setPercentComissao(double percentComissao) { this.percentComissao = percentComissao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() { return nome; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Barbeiro other)) return false;
        return idBarbeiro != null && idBarbeiro.equals(other.idBarbeiro);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idBarbeiro); }
}
