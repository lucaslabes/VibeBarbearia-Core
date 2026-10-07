/* =====================================================================
   Vibe Barbearia — auth.js
   Login simulado, sessão e controle de acesso por perfil.
   A matriz de permissões é a mesma da classe PoliticaAcesso do Core:
     DONO     -> todas
     GERENTE  -> clientes, ver funcionários, editar agenda, caixa completo, relatórios
     BARBEIRO -> registrar atendimento (só os próprios); agenda em modo consulta;
                 caixa e dashboard só com os próprios números
   ===================================================================== */
(function (global) {
    'use strict';
    const Vibe = global.Vibe = global.Vibe || {};
    const R = Vibe.Regras;
    const D = Vibe.Dados;
    const CHAVE_SESSAO = 'vibe.sessao';

    const P = Object.freeze({
        GERENCIAR_CLIENTES: 'GERENCIAR_CLIENTES',
        VISUALIZAR_FUNCIONARIOS: 'VISUALIZAR_FUNCIONARIOS',
        CADASTRAR_FUNCIONARIO: 'CADASTRAR_FUNCIONARIO',
        ALTERAR_COMISSAO: 'ALTERAR_COMISSAO',
        GERENCIAR_GERENTES: 'GERENCIAR_GERENTES',
        EDITAR_AGENDA: 'EDITAR_AGENDA',
        REGISTRAR_ATENDIMENTO: 'REGISTRAR_ATENDIMENTO',
        GERENCIAR_CATALOGO: 'GERENCIAR_CATALOGO',
        VER_CAIXA_COMPLETO: 'VER_CAIXA_COMPLETO',
        VER_RELATORIOS: 'VER_RELATORIOS'
    });
    const MATRIZ = Object.freeze({
        DONO: Object.values(P),
        GERENTE: [P.GERENCIAR_CLIENTES, P.VISUALIZAR_FUNCIONARIOS, P.EDITAR_AGENDA, P.VER_CAIXA_COMPLETO, P.VER_RELATORIOS],
        BARBEIRO: [P.REGISTRAR_ATENDIMENTO]
    });
    const ROTULO_PERFIL = { DONO: 'Dono', GERENTE: 'Gerente', BARBEIRO: 'Barbeiro' };

    /**
     * Páginas da área interna: quem pode abrir cada uma.
     * null = qualquer usuário logado (dashboard, agenda em consulta, caixa próprio, senha).
     */
    const PAGINAS = [
        { arquivo: 'dashboard.html', titulo: 'Dashboard', icone: 'painel', permissao: null },
        { arquivo: 'agenda.html', titulo: 'Agenda', icone: 'agenda', permissao: null },
        { arquivo: 'clientes.html', titulo: 'Clientes', icone: 'clientes', permissao: P.GERENCIAR_CLIENTES },
        { arquivo: 'funcionarios.html', titulo: 'Funcionários', icone: 'equipe', permissao: P.VISUALIZAR_FUNCIONARIOS },
        { arquivo: 'servicos.html', titulo: 'Serviços e produtos', icone: 'tesoura', permissao: P.GERENCIAR_CATALOGO },
        { arquivo: 'atendimento.html', titulo: 'Atendimento', icone: 'atendimento', permissao: P.REGISTRAR_ATENDIMENTO },
        { arquivo: 'caixa.html', titulo: 'Caixa', icone: 'caixa', permissao: null },
        { arquivo: 'relatorios.html', titulo: 'Relatórios', icone: 'relatorios', permissao: P.VER_RELATORIOS },
        { arquivo: 'alterar-senha.html', titulo: 'Alterar senha', icone: 'chave', permissao: null }
    ];

    const sessaoBruta = () => {
        try { return JSON.parse(global.sessionStorage.getItem(CHAVE_SESSAO)); } catch (e) { return null; }
    };

    const Auth = {
        P, MATRIZ, PAGINAS, ROTULO_PERFIL,

        /** Mensagem genérica, como no desktop: não revela se o login existe. */
        entrar(login, senha) {
            const l = String(login || '').trim();
            if (!l || !senha) throw new R.ErroValidacao('Preencha usuário e senha para continuar.');
            // gerente e barbeiro entram com o CPF: aceita com ou sem máscara
            const chave = /^[\d.\-\s]+$/.test(l) ? R.somenteDigitos(l) : l;
            const u = D.buscar('usuarios', (x) => x.login === chave && x.senha === senha)[0];
            if (!u) throw new R.ErroValidacao('Usuário ou senha inválidos.');
            if (u.perfil === 'BARBEIRO') {
                const b = D.obter('barbeiros', u.idBarbeiro);
                if (!b || !b.ativo) throw new R.ErroValidacao('Usuário ou senha inválidos.');
            }
            global.sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify({ idUsuario: u.id, desde: new Date().toISOString() }));
            return Auth.usuario();
        },
        sair() { global.sessionStorage.removeItem(CHAVE_SESSAO); },
        usuario() {
            const s = sessaoBruta();
            if (!s) return null;
            const u = D.obter('usuarios', s.idUsuario);
            if (!u) return null;
            delete u.senha;
            return u;
        },
        pode(usuario, permissao) {
            return !!usuario && (permissao == null || (MATRIZ[usuario.perfil] || []).includes(permissao));
        },
        exigir(usuario, permissao) {
            if (!Auth.pode(usuario, permissao)) {
                throw new R.ErroRegra(`Acesso negado: seu perfil não permite esta operação (${String(permissao).toLowerCase().replace(/_/g, ' ')}).`);
            }
        },
        paginasDo(usuario) { return PAGINAS.filter((p) => Auth.pode(usuario, p.permissao)); },

        /**
         * Protege a página atual: sem login volta ao login; sem permissão
         * volta ao dashboard com aviso. Devolve o usuário logado.
         */
        protegerPagina(permissao) {
            const u = Auth.usuario();
            if (!u) {
                global.location.replace('login.html?voltar=' + encodeURIComponent(global.location.pathname.split('/').pop()));
                return null;
            }
            if (!Auth.pode(u, permissao)) {
                global.sessionStorage.setItem('vibe.aviso', 'Acesso negado: seu perfil não permite abrir esta página.');
                global.location.replace('dashboard.html');
                return null;
            }
            return u;
        },

        /** TelaAlterarSenha + PoliticaSenha */
        alterarSenha(atual, nova, confirmacao) {
            const s = sessaoBruta();
            if (!s) throw new R.ErroRegra('Nenhum usuário logado.');
            R.validarNovaSenha(atual, nova, confirmacao);
            const u = D.obter('usuarios', s.idUsuario);
            if (u.senha !== atual) throw new R.ErroValidacao('A senha atual não confere.', 'atual');
            D.atualizar('usuarios', { id: u.id, senha: nova });
        },

        /**
         * TelaRecuperarSenha em "modo demonstração" (sem SMTP): gera a senha
         * provisória e a devolve para ser exibida na tela.
         */
        recuperarSenha(login) {
            const l = String(login || '').trim();
            if (!l) throw new R.ErroValidacao('Informe o login para recuperar a senha.', 'login');
            const chave = /^[\d.\-\s]+$/.test(l) ? R.somenteDigitos(l) : l;
            const u = D.buscar('usuarios', (x) => x.login === chave)[0];
            if (!u) return { encontrado: false };                       // resposta neutra
            if (!R.isEmailValido(u.email)) return { encontrado: true, semEmail: true };
            const senha = R.gerarSenhaProvisoria();
            D.atualizar('usuarios', { id: u.id, senha });
            return { encontrado: true, senha, emailMascarado: R.mascararEmail(u.email), login: u.login };
        }
    };

    Vibe.Auth = Auth;
})(window);
