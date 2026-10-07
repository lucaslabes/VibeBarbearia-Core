package vibebarbearia.core.service.regras;

import vibebarbearia.core.exception.AcessoNegadoException;
import vibebarbearia.core.model.PerfilUsuario;
import vibebarbearia.core.model.Usuario;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz perfil -> permissões. Antes as regras estavam espalhadas em
 * Usuario.pode*() e em checagens "if (!SessaoUsuario.isDono())" dentro de
 * TelaBarbeiros, TelaRegistroAtendimento e TelaCaixa. Agora há um único ponto
 * de decisão; incluir um novo perfil é só uma nova entrada no mapa (OCP).
 */
public class PoliticaAcesso {

    private final Map<PerfilUsuario, Set<Permissao>> matriz = new EnumMap<>(PerfilUsuario.class);

    public PoliticaAcesso() {
        matriz.put(PerfilUsuario.DONO, EnumSet.allOf(Permissao.class));
        matriz.put(PerfilUsuario.GERENTE, EnumSet.of(
                Permissao.GERENCIAR_CLIENTES, Permissao.VISUALIZAR_FUNCIONARIOS,
                Permissao.EDITAR_AGENDA, Permissao.VER_CAIXA_COMPLETO, Permissao.VER_RELATORIOS));
        matriz.put(PerfilUsuario.BARBEIRO, EnumSet.of(Permissao.REGISTRAR_ATENDIMENTO));
    }

    public boolean pode(Usuario usuario, Permissao permissao) {
        return usuario != null && usuario.getPerfil() != null
                && matriz.getOrDefault(usuario.getPerfil(), Set.of()).contains(permissao);
    }

    public void exigir(Usuario usuario, Permissao permissao) {
        if (!pode(usuario, permissao)) {
            throw new AcessoNegadoException("Acesso negado: seu perfil não permite esta operação ("
                    + permissao.name().toLowerCase().replace('_', ' ') + ").");
        }
    }
}
