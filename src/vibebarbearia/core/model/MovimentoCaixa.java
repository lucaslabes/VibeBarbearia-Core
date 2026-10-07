package vibebarbearia.core.model;

import java.time.LocalDateTime;

/** Entrada no caixa gerada ao finalizar um atendimento. */
public class MovimentoCaixa {
    private Integer idMovimento;
    private LocalDateTime dataHora;
    private Cliente cliente;
    private Barbeiro barbeiro;
    private double valor;
    private double comissao;
    private FormaPagamento formaPagamento;
    private String descricaoServicos;
    private Integer idAgendamento;

    public Integer getIdMovimento() { return idMovimento; }
    public void setIdMovimento(Integer idMovimento) { this.idMovimento = idMovimento; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Barbeiro getBarbeiro() { return barbeiro; }
    public void setBarbeiro(Barbeiro barbeiro) { this.barbeiro = barbeiro; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public double getComissao() { return comissao; }
    public void setComissao(double comissao) { this.comissao = comissao; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public String getDescricaoServicos() { return descricaoServicos; }
    public void setDescricaoServicos(String descricaoServicos) { this.descricaoServicos = descricaoServicos; }
    public Integer getIdAgendamento() { return idAgendamento; }
    public void setIdAgendamento(Integer idAgendamento) { this.idAgendamento = idAgendamento; }

    public boolean pertenceAoBarbeiro(Integer idBarbeiro) {
        return idBarbeiro != null && barbeiro != null && idBarbeiro.equals(barbeiro.getIdBarbeiro());
    }

    public String getNomeCliente() { return cliente != null ? cliente.getNome() : "-"; }
    public String getNomeBarbeiro() { return barbeiro != null ? barbeiro.getNome() : "-"; }
}
