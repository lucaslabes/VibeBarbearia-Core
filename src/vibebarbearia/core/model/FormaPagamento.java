package vibebarbearia.core.model;

/**
 * Formas de pagamento aceitas.
 * Substitui as strings mágicas "Dinheiro", "Cartão" e "PIX" espalhadas em
 * TelaRegistroAtendimento, TelaCaixa e TelaRelatorios do projeto desktop.
 */
public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO("Cartão"),
    PIX("PIX");

    private final String rotulo;

    FormaPagamento(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }

    /** Converte o texto gravado no banco (coluna forma_pagamento) para o enum. */
    public static FormaPagamento deRotulo(String texto) {
        if (texto != null) {
            for (FormaPagamento f : values()) {
                if (f.rotulo.equalsIgnoreCase(texto.trim()) || f.name().equalsIgnoreCase(texto.trim())) {
                    return f;
                }
            }
        }
        throw new IllegalArgumentException("Forma de pagamento desconhecida: " + texto);
    }

    @Override
    public String toString() { return rotulo; }
}
