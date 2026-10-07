package vibebarbearia.core.service.regras;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Hash SHA-256 com salt aleatório, formato "salt:hash" (Base64). */
public class SenhaSha256 implements CodificadorSenha {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String codificar(String senhaPura) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String s = Base64.getEncoder().encodeToString(salt);
        return s + ":" + hash(s, senhaPura);
    }

    @Override
    public boolean confere(String senhaPura, String armazenada) {
        if (armazenada == null || !armazenada.contains(":") || senhaPura == null) return false;
        String[] p = armazenada.split(":", 2);
        return MessageDigest.isEqual(hash(p[0], senhaPura).getBytes(StandardCharsets.UTF_8),
                p[1].getBytes(StandardCharsets.UTF_8));
    }

    private static String hash(String salt, String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(md.digest(senha.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
