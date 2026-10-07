package vibebarbearia.core.repository;

/** CRUD completo: salvar = inserir (id nulo) ou atualizar (id preenchido). */
public interface Repositorio<T> extends RepositorioLeitura<T> {
    T salvar(T entidade);
    void excluir(int id);
}
