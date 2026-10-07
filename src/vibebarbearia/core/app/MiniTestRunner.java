package vibebarbearia.core.app;

import java.util.Objects;

/** Executor de testes mínimo (sem JUnit): imprime PASS/FAIL por regra de negócio. */
public class MiniTestRunner {

    @FunctionalInterface
    public interface Teste { void executar() throws Exception; }

    private int passou;
    private int falhou;
    private String grupoAtual = "";

    public void grupo(String nome) {
        grupoAtual = nome;
        System.out.println();
        System.out.println("== " + nome + " ==");
    }

    public void teste(String descricao, Teste t) {
        try {
            t.executar();
            passou++;
            System.out.println("[PASS] " + descricao);
        } catch (Throwable e) {
            falhou++;
            System.out.println("[FAIL] " + descricao + "  -> " + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + " (grupo " + grupoAtual + ")");
        }
    }

    public int resumo() {
        System.out.println();
        System.out.println("==================================================");
        System.out.printf("Total: %d | PASS: %d | FAIL: %d%n", passou + falhou, passou, falhou);
        System.out.println(falhou == 0 ? "RESULTADO: TODAS AS REGRAS DE NEGÓCIO OK" : "RESULTADO: HÁ FALHAS");
        System.out.println("==================================================");
        return falhou;
    }

    // ---------- asserções ----------
    public static void verdadeiro(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    public static void igual(Object esperado, Object obtido) {
        if (!Objects.equals(esperado, obtido)) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + obtido + ">");
        }
    }

    public static void igual(double esperado, double obtido) {
        if (Math.abs(esperado - obtido) > 0.001) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + obtido + ">");
        }
    }

    /** Garante que a ação lança a exceção esperada; devolve a mensagem para conferência. */
    public static String lanca(Class<? extends Throwable> tipo, Teste acao) {
        try {
            acao.executar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) return e.getMessage();
            throw new AssertionError("esperava " + tipo.getSimpleName() + " mas veio " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
        }
        throw new AssertionError("esperava " + tipo.getSimpleName() + " mas nada foi lançado");
    }
}
