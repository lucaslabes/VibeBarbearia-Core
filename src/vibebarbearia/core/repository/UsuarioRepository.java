package vibebarbearia.core.repository;

import vibebarbearia.core.model.PerfilUsuario;
import vibebarbearia.core.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends Repositorio<Usuario> {
    Optional<Usuario> buscarPorLogin(String login);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorIdBarbeiro(int idBarbeiro);
    List<Usuario> listarPorPerfil(PerfilUsuario perfil);

    /** @param exceptIdUsuario ignora este id (edição); pode ser null. */
    boolean loginExiste(String login, Integer exceptIdUsuario);
}
