package vibebarbearia.core.validation;

import vibebarbearia.core.exception.ValidacaoException;
import vibebarbearia.core.util.TextoUtil;

/**
 * Validações reutilizáveis. Antes estavam duplicadas em
 * TelaClientes.abrirCadastroCliente e TelaBarbeiros.abrirFormularioFuncionario
 * (telefone 10/11 dígitos, CPF 11 dígitos, e-mail contém "@" e tem 5+ caracteres,
 * comissão 0..100, todas copiadas 2 ou 3 vezes).
 */
public final class Validador {
    private Validador() {}

    public static String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new ValidacaoException(mensagem);
        return valor.trim();
    }

    public static <T> T obrigatorio(T valor, String mensagem) {
        if (valor == null) throw new ValidacaoException(mensagem);
        return valor;
    }

    /** Retorna o telefone só com dígitos, validando 10 ou 11 dígitos. */
    public static String telefone(String bruto) {
        String digitos = TextoUtil.apenasDigitos(bruto);
        if (digitos.isEmpty()) throw new ValidacaoException("Telefone é obrigatório.");
        if (digitos.length() < RegrasNegocio.TELEFONE_MIN_DIGITOS
                || digitos.length() > RegrasNegocio.TELEFONE_MAX_DIGITOS) {
            throw new ValidacaoException("Telefone deve ter "
                    + RegrasNegocio.TELEFONE_MIN_DIGITOS + " ou "
                    + RegrasNegocio.TELEFONE_MAX_DIGITOS + " dígitos.");
        }
        return digitos;
    }

    /** CPF com exatamente 11 dígitos (é o login de gerente e barbeiro). */
    public static String cpf(String bruto) {
        String digitos = TextoUtil.apenasDigitos(bruto);
        if (digitos.length() != RegrasNegocio.CPF_DIGITOS) {
            throw new ValidacaoException("CPF deve ter " + RegrasNegocio.CPF_DIGITOS + " dígitos.");
        }
        return digitos;
    }

    /** E-mail obrigatório (necessário para recuperar senha). */
    public static String emailObrigatorio(String email) {
        if (!isEmailValido(email)) throw new ValidacaoException("Informe um e-mail válido.");
        return email.trim();
    }

    /** E-mail opcional: vazio vira null; preenchido precisa ser válido. */
    public static String emailOpcional(String email) {
        if (email == null || email.isBlank()) return null;
        return emailObrigatorio(email);
    }

    public static boolean isEmailValido(String email) {
        if (email == null) return false;
        String e = email.trim();
        int arroba = e.indexOf('@');
        return e.length() >= RegrasNegocio.EMAIL_MIN_CARACTERES
                && arroba > 0 && arroba < e.length() - 1;
    }

    public static double comissao(double percentual) {
        if (Double.isNaN(percentual)
                || percentual < RegrasNegocio.COMISSAO_MIN_PERCENT
                || percentual > RegrasNegocio.COMISSAO_MAX_PERCENT) {
            throw new ValidacaoException("Comissão deve ser um número entre 0 e 100.");
        }
        return percentual;
    }

    public static double precoNaoNegativo(double preco) {
        if (Double.isNaN(preco) || preco < 0) throw new ValidacaoException("Preço inválido.");
        return preco;
    }
}
