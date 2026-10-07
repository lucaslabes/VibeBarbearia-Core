package vibebarbearia.core.service.relatorio;

import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.RepositoryFactory;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;
import vibebarbearia.core.util.Periodo;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Contexto do Strategy: escolhe o gerador pelo tipo e entrega os dados do período. */
public class RelatorioService {

    private final RepositoryFactory repos;
    private final PoliticaAcesso acesso;
    private final Map<TipoRelatorio, GeradorRelatorio> geradores = new EnumMap<>(TipoRelatorio.class);

    public RelatorioService(RepositoryFactory repos, PoliticaAcesso acesso, List<GeradorRelatorio> estrategias) {
        this.repos = repos;
        this.acesso = acesso;
        estrategias.forEach(g -> geradores.put(g.tipo(), g));
    }

    /** Conjunto padrão com os 6 relatórios do sistema desktop. */
    public static List<GeradorRelatorio> estrategiasPadrao() {
        return List.of(new RelatorioResumo(), new RelatorioBarbeiros(), new RelatorioPagamentos(),
                new RelatorioAgenda(), new RelatorioClientes(), new RelatorioServicos());
    }

    public ResultadoRelatorio gerar(Usuario solicitante, TipoRelatorio tipo, Periodo periodo) {
        acesso.exigir(solicitante, Permissao.VER_RELATORIOS);
        GeradorRelatorio g = geradores.get(tipo);
        if (g == null) throw new RegraNegocioException("Relatório não disponível: " + tipo);
        DadosRelatorio dados = new DadosRelatorio(periodo,
                repos.movimentos().listarPorPeriodo(periodo),
                repos.agendamentos().listarPorPeriodo(periodo),
                repos.barbeiros().listarTodos(),
                repos.clientes().listarTodos());
        return g.gerar(dados);
    }
}
