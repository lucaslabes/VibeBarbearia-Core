package vibebarbearia.core.validation;

/**
 * Constantes de negócio centralizadas.
 * Elimina os "números mágicos" do desktop: 10/11 dígitos de telefone,
 * 11 do CPF, senha mínima 3, senha padrão "123", comissão padrão 40,
 * 1 ponto a cada R$ 10, senha provisória de 8 caracteres, e-mail com 5+ chars.
 */
public final class RegrasNegocio {
    private RegrasNegocio() {}

    public static final int TELEFONE_MIN_DIGITOS = 10;
    public static final int TELEFONE_MAX_DIGITOS = 11;
    public static final int CPF_DIGITOS = 11;
    public static final int EMAIL_MIN_CARACTERES = 5;

    public static final int SENHA_MIN_CARACTERES = 3;
    public static final String SENHA_INICIAL_PADRAO = "123";
    public static final int SENHA_PROVISORIA_TAMANHO = 8;
    public static final String SENHA_PROVISORIA_ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public static final double COMISSAO_PADRAO_PERCENT = 40.0;
    public static final double COMISSAO_MIN_PERCENT = 0.0;
    public static final double COMISSAO_MAX_PERCENT = 100.0;

    /** 1 ponto de fidelidade a cada R$ 10,00 gastos. */
    public static final double VALOR_POR_PONTO_FIDELIDADE = 10.0;

    /** Tolerância usada ao comparar valores monetários (meio centavo). */
    public static final double TOLERANCIA_VALOR = 0.005;

    /** Duração assumida de um atendimento para detectar conflito de horário. */
    public static final int DURACAO_ATENDIMENTO_MINUTOS = 30;
}
