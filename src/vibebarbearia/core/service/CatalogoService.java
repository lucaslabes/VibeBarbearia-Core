package vibebarbearia.core.service;

import vibebarbearia.core.exception.EntidadeNaoEncontradaException;
import vibebarbearia.core.model.Servico;
import vibebarbearia.core.model.TipoItem;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.ServicoRepository;
import vibebarbearia.core.service.regras.Permissao;
import vibebarbearia.core.service.regras.PoliticaAcesso;
import vibebarbearia.core.util.ConversorEntrada;
import vibebarbearia.core.validation.Validador;

import java.util.List;

/** Catálogo de serviços/produtos (de TelaRegistroAtendimento.abrirFormularioItem). */
public class CatalogoService {

    private final ServicoRepository servicos;
    private final PoliticaAcesso acesso;

    public CatalogoService(ServicoRepository servicos, PoliticaAcesso acesso) {
        this.servicos = servicos;
        this.acesso = acesso;
    }

    public Servico salvarItem(Usuario solicitante, Integer idExistente, String nome, String precoTexto, TipoItem tipo) {
        acesso.exigir(solicitante, Permissao.GERENCIAR_CATALOGO);
        Double preco = Validador.obrigatorio(ConversorEntrada.valorMonetario(precoTexto), "Informe o preço.");
        Servico s = idExistente == null ? new Servico() : servicos.buscarPorId(idExistente)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Item não encontrado."));
        s.setNome(Validador.obrigatorio(nome, "Informe o nome do item."));
        s.setPreco(Validador.precoNaoNegativo(preco));
        s.setTipo(tipo);
        return servicos.salvar(s);
    }

    public void excluirItem(Usuario solicitante, int idServico) {
        acesso.exigir(solicitante, Permissao.GERENCIAR_CATALOGO);
        servicos.excluir(idServico);
    }

    public List<Servico> listar() { return servicos.listarTodos(); }
}
