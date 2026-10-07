package vibebarbearia.core.model;

/**
 * Usuário do sistema (login) com perfil de acesso.
 * As regras de permissão (antes métodos podeAcessar* nesta classe)
 * foram movidas para service.regras.PoliticaAcesso (SRP).
 */
public class Usuario {
    private Integer idUsuario;
    private String login;
    private String senha;
    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private PerfilUsuario perfil;
    /** Preenchido quando perfil = BARBEIRO. */
    private Integer idBarbeiro;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }
    public Integer getIdBarbeiro() { return idBarbeiro; }
    public void setIdBarbeiro(Integer idBarbeiro) { this.idBarbeiro = idBarbeiro; }

    public boolean isDono() { return perfil == PerfilUsuario.DONO; }
    public boolean isGerente() { return perfil == PerfilUsuario.GERENTE; }
    public boolean isBarbeiro() { return perfil == PerfilUsuario.BARBEIRO; }

    public boolean temEmail() { return email != null && !email.isBlank(); }

    @Override
    public String toString() { return nome; }
}
