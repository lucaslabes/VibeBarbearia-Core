package vibebarbearia.core.model;

import java.time.LocalDate;
import java.util.Objects;

/** Cliente da barbearia (entidade de domínio, sem dependência de UI). */
public class Cliente {
    private Integer idCliente;
    private String nome;
    private String telefone;
    private String email;
    private LocalDate dataNascimento;
    private int pontosFidelidade;
    /** Data do último atendimento concluído (antes era String "dd/MM/yyyy"). */
    private LocalDate ultimoCorte;

    public Cliente() {}

    public Cliente(String nome, String telefone, String email) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public int getPontosFidelidade() { return pontosFidelidade; }
    public void setPontosFidelidade(int pontosFidelidade) { this.pontosFidelidade = pontosFidelidade; }
    public LocalDate getUltimoCorte() { return ultimoCorte; }
    public void setUltimoCorte(LocalDate ultimoCorte) { this.ultimoCorte = ultimoCorte; }

    /** Regra de domínio: acumula pontos de fidelidade (nunca negativos). */
    public void adicionarPontos(int pontos) {
        if (pontos < 0) throw new IllegalArgumentException("Pontos não podem ser negativos");
        this.pontosFidelidade += pontos;
    }

    @Override
    public String toString() { return nome; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente other)) return false;
        return idCliente != null && idCliente.equals(other.idCliente);
    }

    @Override
    public int hashCode() { return Objects.hashCode(idCliente); }
}
