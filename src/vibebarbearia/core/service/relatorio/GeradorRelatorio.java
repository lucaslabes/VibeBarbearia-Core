package vibebarbearia.core.service.relatorio;

/**
 * Strategy: cada relatório é uma classe. Antes, TelaRelatorios.gerarRelatorio
 * fazia um switch sobre Strings e cada relatorioXxx() misturava cálculo
 * com preenchimento da JTable. Novo relatório = nova classe (OCP).
 */
public interface GeradorRelatorio {
    TipoRelatorio tipo();
    ResultadoRelatorio gerar(DadosRelatorio dados);
}
