package vibebarbearia.core.repository.jdbc;

import vibebarbearia.core.model.FormaPagamento;
import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.repository.BarbeiroRepository;
import vibebarbearia.core.repository.ClienteRepository;
import vibebarbearia.core.repository.MovimentoCaixaRepository;
import vibebarbearia.core.util.Periodo;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static vibebarbearia.core.repository.jdbc.JdbcExecutor.getIntOuNulo;
import static vibebarbearia.core.repository.jdbc.JdbcExecutor.setIntOuNulo;

public class MovimentoCaixaRepositoryJdbc implements MovimentoCaixaRepository {

    private final JdbcExecutor jdbc;
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;

    public MovimentoCaixaRepositoryJdbc(JdbcExecutor jdbc, ClienteRepository clientes, BarbeiroRepository barbeiros) {
        this.jdbc = jdbc;
        this.clientes = clientes;
        this.barbeiros = barbeiros;
    }

    @Override
    public MovimentoCaixa registrar(MovimentoCaixa m) {
        m.setIdMovimento(jdbc.inserir("INSERT INTO movimento_caixa (data_hora, id_cliente, id_barbeiro, valor, comissao, "
                + "forma_pagamento, descricao_servicos, id_agendamento) VALUES (?,?,?,?,?,?,?,?)", ps -> {
            ps.setTimestamp(1, Timestamp.valueOf(m.getDataHora()));
            setIntOuNulo(ps, 2, m.getCliente() != null ? m.getCliente().getIdCliente() : null);
            setIntOuNulo(ps, 3, m.getBarbeiro() != null ? m.getBarbeiro().getIdBarbeiro() : null);
            ps.setDouble(4, m.getValor());
            ps.setDouble(5, m.getComissao());
            ps.setString(6, m.getFormaPagamento().getRotulo());
            ps.setString(7, m.getDescricaoServicos());
            setIntOuNulo(ps, 8, m.getIdAgendamento());
        }));
        return m;
    }

    @Override
    public List<MovimentoCaixa> listarTodos() {
        return jdbc.listar("SELECT * FROM movimento_caixa ORDER BY data_hora", this::mapear);
    }

    @Override
    public List<MovimentoCaixa> listarPorPeriodo(Periodo p) {
        return jdbc.listar("SELECT * FROM movimento_caixa WHERE DATE(data_hora) BETWEEN ? AND ? ORDER BY data_hora",
                ps -> { ps.setDate(1, Date.valueOf(p.inicio())); ps.setDate(2, Date.valueOf(p.fim())); }, this::mapear);
    }

    @Override
    public boolean existePorBarbeiro(int idBarbeiro) {
        return jdbc.buscarUm("SELECT 1 FROM movimento_caixa WHERE id_barbeiro = ? LIMIT 1",
                ps -> ps.setInt(1, idBarbeiro), rs -> true).isPresent();
    }

    @Override
    public Optional<MovimentoCaixa> buscarPorId(int id) {
        return jdbc.buscarUm("SELECT * FROM movimento_caixa WHERE id_movimento = ?", ps -> ps.setInt(1, id), this::mapear);
    }

    private MovimentoCaixa mapear(ResultSet rs) throws SQLException {
        MovimentoCaixa m = new MovimentoCaixa();
        m.setIdMovimento(rs.getInt("id_movimento"));
        m.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        Integer idCli = getIntOuNulo(rs, "id_cliente");
        if (idCli != null) m.setCliente(clientes.buscarPorId(idCli).orElse(null));
        Integer idBar = getIntOuNulo(rs, "id_barbeiro");
        if (idBar != null) m.setBarbeiro(barbeiros.buscarPorId(idBar).orElse(null));
        m.setValor(rs.getDouble("valor"));
        m.setComissao(rs.getDouble("comissao"));
        m.setFormaPagamento(FormaPagamento.deRotulo(rs.getString("forma_pagamento")));
        m.setDescricaoServicos(rs.getString("descricao_servicos"));
        m.setIdAgendamento(getIntOuNulo(rs, "id_agendamento"));
        return m;
    }
}
