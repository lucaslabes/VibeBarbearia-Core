package vibebarbearia.core.app;

import vibebarbearia.core.repository.RepositoryFactory;
import vibebarbearia.core.repository.jdbc.ConexaoMySQL;
import vibebarbearia.core.repository.jdbc.JdbcRepositoryFactory;
import vibebarbearia.core.repository.memoria.MemoriaRepositoryFactory;
import vibebarbearia.core.service.*;
import vibebarbearia.core.service.regras.*;
import vibebarbearia.core.service.relatorio.RelatorioService;
import vibebarbearia.core.validation.PoliticaSenha;

import java.time.Clock;

/**
 * Composition Root / Factory dos serviços: o único lugar que conhece as
 * implementações concretas. Desktop (Swing) e futura Web criam um
 * VibeBarbeariaCore e usam apenas os serviços — sem new XxxDAO() nas telas
 * e sem o Singleton BancoEmMemoria.getInstancia().
 */
public class VibeBarbeariaCore {

    private final RepositoryFactory repositorios;
    private final ClienteService clientes;
    private final FuncionarioService funcionarios;
    private final CatalogoService catalogo;
    private final AgendamentoService agenda;
    private final AtendimentoService atendimento;
    private final CaixaService caixa;
    private final AutenticacaoService autenticacao;
    private final DashboardService dashboard;
    private final RelatorioService relatorios;

    public VibeBarbeariaCore(RepositoryFactory r, Clock relogio, CodificadorSenha codificador,
                             GeradorSenha gerador, NotificadorSenha notificador) {
        this.repositorios = r;
        PoliticaAcesso acesso = new PoliticaAcesso();
        clientes = new ClienteService(r.clientes(), r.agendamentos(), acesso);
        funcionarios = new FuncionarioService(r.barbeiros(), r.usuarios(), r.agendamentos(), r.movimentos(), acesso, codificador);
        catalogo = new CatalogoService(r.servicos(), acesso);
        agenda = new AgendamentoService(r.agendamentos(), r.clientes(), r.barbeiros(), acesso);
        atendimento = new AtendimentoService(r.agendamentos(), r.clientes(), r.movimentos(), acesso,
                new ComissaoPercentualBarbeiro(), new FidelidadePorValor(), relogio);
        caixa = new CaixaService(r.movimentos(), acesso);
        autenticacao = new AutenticacaoService(r.usuarios(), codificador, new PoliticaSenha(), gerador, notificador);
        dashboard = new DashboardService(r.movimentos(), r.agendamentos(), relogio);
        relatorios = new RelatorioService(r, acesso, RelatorioService.estrategiasPadrao());
    }

    /** Configuração para testes/demonstração: tudo em memória. */
    public static VibeBarbeariaCore emMemoria(Clock relogio) {
        return new VibeBarbeariaCore(new MemoriaRepositoryFactory(), relogio, new SenhaTextoPlano(),
                new GeradorSenhaAleatoria(), new NotificadorModoDemonstracao());
    }

    /** Configuração de produção: MySQL (mesmo schema sql/vibebarbearia.sql do desktop). */
    public static VibeBarbeariaCore comMySQL(NotificadorSenha notificador) {
        return new VibeBarbeariaCore(new JdbcRepositoryFactory(ConexaoMySQL.daConfiguracao()),
                Clock.systemDefaultZone(), new SenhaTextoPlano(), new GeradorSenhaAleatoria(), notificador);
    }

    public RepositoryFactory repositorios() { return repositorios; }
    public ClienteService clientes() { return clientes; }
    public FuncionarioService funcionarios() { return funcionarios; }
    public CatalogoService catalogo() { return catalogo; }
    public AgendamentoService agenda() { return agenda; }
    public AtendimentoService atendimento() { return atendimento; }
    public CaixaService caixa() { return caixa; }
    public AutenticacaoService autenticacao() { return autenticacao; }
    public DashboardService dashboard() { return dashboard; }
    public RelatorioService relatorios() { return relatorios; }
}
