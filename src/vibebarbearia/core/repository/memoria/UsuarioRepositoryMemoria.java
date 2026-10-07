package vibebarbearia.core.repository.memoria;

import vibebarbearia.core.model.PerfilUsuario;
import vibebarbearia.core.model.Usuario;
import vibebarbearia.core.repository.UsuarioRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UsuarioRepositoryMemoria extends RepositorioMemoria<Usuario> implements UsuarioRepository {

    public UsuarioRepositoryMemoria() { super(Usuario::getIdUsuario, Usuario::setIdUsuario); }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return listarTodos().stream().filter(u -> Objects.equals(u.getLogin(), login)).findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return listarTodos().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim())).findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorIdBarbeiro(int idBarbeiro) {
        return listarTodos().stream().filter(u -> Integer.valueOf(idBarbeiro).equals(u.getIdBarbeiro())).findFirst();
    }

    @Override
    public List<Usuario> listarPorPerfil(PerfilUsuario perfil) {
        return listarTodos().stream().filter(u -> u.getPerfil() == perfil)
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Override
    public boolean loginExiste(String login, Integer exceptIdUsuario) {
        return listarTodos().stream().anyMatch(u -> Objects.equals(u.getLogin(), login)
                && (exceptIdUsuario == null || !exceptIdUsuario.equals(u.getIdUsuario())));
    }
}
