package vibebarbearia.core.model;

/**
 * Resultado da remoção de um barbeiro.
 * Substitui as strings "EXCLUIDO" / "INATIVADO" / "NADA" retornadas por
 * BancoEmMemoria.removerBarbeiroComUsuario e BarbeiroDAO.excluirComUsuario.
 */
public enum ResultadoRemocaoBarbeiro {
    /** Sem histórico: barbeiro e login apagados. */
    EXCLUIDO,
    /** Com histórico (agenda/caixa): login removido e barbeiro inativado. */
    INATIVADO
}
