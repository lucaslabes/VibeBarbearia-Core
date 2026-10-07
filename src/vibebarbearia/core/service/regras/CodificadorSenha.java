package vibebarbearia.core.service.regras;

/** Strategy de armazenamento de senha (permite migrar de texto puro para hash sem mexer no serviço). */
public interface CodificadorSenha {
    String codificar(String senhaPura);
    boolean confere(String senhaPura, String armazenada);
}
