package vibebarbearia.core.service;

import vibebarbearia.core.exception.RegraNegocioException;
import vibebarbearia.core.exception.ValidacaoException;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.UsuarioRepository;
import vibebarbearia.core.service.regras.CodificadorSenha;
import vibebarbearia.core.service.regras.GeradorSenha;
import vibebarbearia.core.service.regras.NotificadorSenha;
import vibebarbearia.core.util.TextoUtil;
import vibebarbearia.core.validation.PoliticaSenha;

import java.util.Optional;

/**
 * Login, alteração e recuperação de senha. Extraído de
 * TelaLogin.realizarLogin, TelaAlterarSenha.abrir (listener do Salvar) e
 * TelaRecuperarSenha.processarRecuperacao.
 * Não guarda estado global (o antigo SessaoUsuario estático não funciona
 * num servidor web com vários usuários): devolve o Usuario autenticado.
 */
public class AutenticacaoService {

    private final UsuarioRepository usuarios;
    private final CodificadorSenha codificador;
    private final PoliticaSenha politicaSenha;
    private final GeradorSenha geradorSenha;
    private final NotificadorSenha notificador;

    public AutenticacaoService(UsuarioRepository usuarios, CodificadorSenha codificador, PoliticaSenha politicaSenha,
                               GeradorSenha geradorSenha, NotificadorSenha notificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.politicaSenha = politicaSenha;
        this.geradorSenha = geradorSenha;
        this.notificador = notificador;
    }

    public Usuario autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            throw new ValidacaoException("Preencha usuário e senha para continuar.");
        }
        return usuarios.buscarPorLogin(login.trim())
                .filter(u -> codificador.confere(senha.trim(), u.getSenha()))
                .orElseThrow(() -> new RegraNegocioException("Usuário ou senha inválidos."));
    }

    public void alterarSenha(Usuario logado, String atual, String nova, String confirmacao) {
        politicaSenha.validarNovaSenha(atual, nova, confirmacao);
        Usuario noBanco = usuarios.buscarPorId(logado.getIdUsuario())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        if (!codificador.confere(atual.trim(), noBanco.getSenha())) {
            throw new RegraNegocioException("A senha atual não confere.");
        }
        noBanco.setSenha(codificador.codificar(nova.trim()));
        usuarios.salvar(noBanco);
    }

    /**
     * Gera senha provisória e tenta enviá-la. Login inexistente devolve
     * Optional.empty() (a tela mostra mensagem neutra, sem revelar se o login existe).
     */
    public Optional<ResultadoRecuperacao> recuperarSenha(String login) {
        Optional<Usuario> opt = usuarios.buscarPorLogin(login == null ? "" : login.trim());
        if (opt.isEmpty()) return Optional.empty();
        Usuario u = opt.get();
        if (!u.temEmail()) {
            throw new RegraNegocioException("Este usuário não possui e-mail cadastrado. Peça ao dono para atualizar o cadastro.");
        }
        String provisoria = geradorSenha.gerar();
        u.setSenha(codificador.codificar(provisoria));
        usuarios.salvar(u);
        boolean enviada = notificador.enviarSenhaProvisoria(u, provisoria);
        return Optional.of(new ResultadoRecuperacao(enviada, TextoUtil.mascararEmail(u.getEmail()),
                enviada ? null : provisoria));
    }
}
