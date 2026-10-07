package vibebarbearia.core.service;

import vibebarbearia.core.model.FormaPagamento;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.util.Periodo;
import vibebarbearia.core.util.TextoUtil;
import vibebarbearia.core.validation.RegrasNegocio;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * Critérios de consulta do caixa (Builder fluente + Specification via Predicate).
 * Substitui os métodos passaFiltroCliente/Barbeiro/Forma/Valor/passaBusca de TelaCaixa,
 * que dependiam do texto "Todos..." do JComboBox para saber se o filtro estava ativo.
 */
public class FiltroCaixa {

    private final Periodo periodo;
    private Integer idCliente;
    private Integer idBarbeiro;
    private FormaPagamento forma;
    private Double valor;
    private String termo = "";

    public FiltroCaixa(Periodo periodo) { this.periodo = periodo; }

    public FiltroCaixa cliente(Integer id) { this.idCliente = id; return this; }
    public FiltroCaixa barbeiro(Integer id) { this.idBarbeiro = id; return this; }
    public FiltroCaixa forma(FormaPagamento f) { this.forma = f; return this; }
    public FiltroCaixa valor(Double v) { this.valor = v; return this; }
    public FiltroCaixa busca(String t) { this.termo = TextoUtil.normalizarBusca(t); return this; }

    public Periodo getPeriodo() { return periodo; }

    public Predicate<MovimentoCaixa> comoPredicado() {
        Predicate<MovimentoCaixa> p = m -> periodo.contem(m.getDataHora());
        if (idCliente != null) p = p.and(m -> m.getCliente() != null && idCliente.equals(m.getCliente().getIdCliente()));
        if (idBarbeiro != null) p = p.and(m -> m.pertenceAoBarbeiro(idBarbeiro));
        if (forma != null) p = p.and(m -> m.getFormaPagamento() == forma);
        if (valor != null) p = p.and(m -> Math.abs(m.getValor() - valor) < RegrasNegocio.TOLERANCIA_VALOR);
        if (!termo.isEmpty()) p = p.and(this::passaBusca);
        return p;
    }

    private boolean passaBusca(MovimentoCaixa m) {
        return TextoUtil.contemIgnorandoCaixa(m.getNomeCliente(), termo)
                || TextoUtil.contemIgnorandoCaixa(m.getNomeBarbeiro(), termo)
                || (m.getFormaPagamento() != null && TextoUtil.contemIgnorandoCaixa(m.getFormaPagamento().getRotulo(), termo))
                || TextoUtil.contemIgnorandoCaixa(m.getDescricaoServicos(), termo)
                || String.format(Locale.US, "%.2f", m.getValor()).contains(termo.replace(',', '.'));
    }
}
