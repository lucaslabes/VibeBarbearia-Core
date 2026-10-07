package vibebarbearia.core.service.regras;

/** Gera senhas provisórias (injetável para testes determinísticos). */
@FunctionalInterface
public interface GeradorSenha {
    String gerar();
}
