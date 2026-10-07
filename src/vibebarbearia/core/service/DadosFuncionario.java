package vibebarbearia.core.service;

/**
 * Dados de entrada do formulário de funcionário (Parameter Object).
 * Substitui as ~15 variáveis locais lidas de JTextField dentro de
 * TelaBarbeiros.abrirFormularioFuncionario.
 *
 * @param comissao texto digitado (ex.: "40", "40%", "37,5"); ignorado para gerente
 * @param senha    vazia => senha inicial padrão
 */
public record DadosFuncionario(String nome, String telefone, String cpf, String email,
                               String senha, String comissao, boolean ativo) {
}
