package vibebarbearia.core.service;

import vibebarbearia.core.model.MovimentoCaixa;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.MovimentoCaixaRepository;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;

import java.util.Comparator;
import java.util.List;

/** Consulta do caixa com a regra "barbeiro vê só os próprios movimentos". */
public class CaixaService {

    private final MovimentoCaixaRepository movimentos;
    private final PoliticaAcesso acesso;

    public CaixaService(MovimentoCaixaRepository movimentos, PoliticaAcesso acesso) {
        this.movimentos = movimentos;
        this.acesso = acesso;
    }

    public ResumoCaixa consultar(Usuario solicitante, FiltroCaixa filtro) {
        if (!acesso.pode(solicitante, Permissao.VER_CAIXA_COMPLETO)) {
            Integer proprio = solicitante.getIdBarbeiro();
            filtro.barbeiro(proprio != null ? proprio : -1); // sem vínculo => nada
        }
        List<MovimentoCaixa> lista = movimentos.listarPorPeriodo(filtro.getPeriodo()).stream()
                .filter(filtro.comoPredicado())
                .sorted(Comparator.comparing(MovimentoCaixa::getDataHora))
                .toList();
        return ResumoCaixa.de(lista);
    }
}
