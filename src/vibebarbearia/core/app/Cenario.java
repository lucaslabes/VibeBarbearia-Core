package vibebarbearia.core.app;

import vibebarbearia.core.model.*;
import vibebarbearia.core.service.DadosFuncionario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Massa de dados de teste em memória (equivalente aos INSERTs de sql/vibebarbearia.sql).
 * Cada teste cria um Cenario novo, garantindo isolamento.
 */
public class Cenario {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 7, 10, 0);

    public final VibeBarbeariaCore core;
    public final Usuario dono;
    public final Usuario gerente;
    public final Barbeiro thiago;
    public final Barbeiro rafael;
    public final Usuario loginThiago;
    public final Usuario loginRafael;
    public final Cliente joao;
    public final Cliente maria;
    public final Servico corte;
    public final Servico barba;
    public final Servico pomada;

    public Cenario() {
        core = VibeBarbeariaCore.emMemoria(Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO));

        dono = new Usuario();
        dono.setLogin("admin");
        dono.setSenha("admin");
        dono.setNome("Administrador");
        dono.setPerfil(PerfilUsuario.DONO);
        dono.setEmail("dono@vibe.com");
        core.repositorios().usuarios().salvar(dono);

        gerente = core.funcionarios().cadastrarGerente(dono, new DadosFuncionario(
                "Lucas Labes", "(11) 95555-5555", "555.555.555-55", "lucas@vibe.com", "", null, true));
        thiago = core.funcionarios().cadastrarBarbeiro(dono, new DadosFuncionario(
                "Thiago Corte", "11911111111", "11111111111", "thiago@vibe.com", "", "40", true));
        rafael = core.funcionarios().cadastrarBarbeiro(dono, new DadosFuncionario(
                "Rafael Navalha", "11922222222", "22222222222", "rafael@vibe.com", "", "50", true));
        loginThiago = core.repositorios().usuarios().buscarPorIdBarbeiro(thiago.getIdBarbeiro()).orElseThrow();
        loginRafael = core.repositorios().usuarios().buscarPorIdBarbeiro(rafael.getIdBarbeiro()).orElseThrow();

        joao = core.clientes().cadastrar(gerente, "João Silva", "(11) 98888-7777", "joao@email.com");
        maria = core.clientes().cadastrar(gerente, "Maria Souza", "1133334444", "");

        corte = core.catalogo().salvarItem(dono, null, "Corte", "R$ 45,00", TipoItem.SERVICO);
        barba = core.catalogo().salvarItem(dono, null, "Barba", "30", TipoItem.SERVICO);
        pomada = core.catalogo().salvarItem(dono, null, "Pomada", "40,00", TipoItem.PRODUTO);
    }

    public Agendamento agendar(Cliente c, Barbeiro b, int hora, int minuto) {
        return core.agenda().agendar(gerente, c.getIdCliente(), b.getIdBarbeiro(),
                AGORA.toLocalDate().atTime(hora, minuto));
    }
}
