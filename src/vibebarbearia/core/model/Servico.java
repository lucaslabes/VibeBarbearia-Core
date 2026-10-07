package vibebarbearia.core.model;

import java.util.Objects;

/** Item do catálogo (serviço ou produto) com preço. */
public class Servico {
    private Integer idServico;
    private String nome;
    private double preco;
    private TipoItem tipo = TipoItem.SERVICO;

    public Servico() {}

    public Servico(String nome, double preco, TipoItem tipo) {
        this.nome = nome;
        this.preco = preco;
        this.tipo = tipo != null ? tipo : TipoItem.SERVICO;
    }

    public Integer getIdServico() { return idServico; }
    public void setIdServico(Integer idServico) { this.idServico = idServico; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public TipoItem getTipo() { return tipo; }
    public void setTipo(TipoItem tipo) { this.tipo = tipo != null ? tipo : TipoItem.SERVICO; }

    public boolean isProduto() { return tipo == TipoItem.PRODUTO; }

    /** Texto gravado no caixa / relatórios (mesma regra de Servico.getDescricaoCaixa original). */
    public String getDescricaoCaixa() {
        return (isProduto() ? "Produto: " : "") + nome;
    }

    @Override
    public String toString() { return nome; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servico other)) return false;
        return idServico != null && idServico.equals(other.idServico);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idServico); }
}
