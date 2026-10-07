/* =====================================================================
   Vibe Barbearia — ui.js
   Funções de interface reutilizadas por todas as páginas:
   ícones, tema, mensagens (toast), confirmação, erros de formulário,
   máscaras de digitação e montagem da área interna (menu por perfil).
   ===================================================================== */
(function (global) {
    'use strict';
    const Vibe = global.Vibe = global.Vibe || {};
    const doc = global.document;
    const $ = (sel, raiz = doc) => raiz.querySelector(sel);
    const $$ = (sel, raiz = doc) => Array.from(raiz.querySelectorAll(sel));

    /** Escapa texto antes de colocar em innerHTML (evita injeção de HTML/XSS). */
    const esc = (s) => String(s == null ? '' : s)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');

    // ---------- ícones SVG (traço simples, herdam a cor do texto) ----------
    const P = {
        painel: '<rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/>',
        agenda: '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
        clientes: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
        equipe: '<circle cx="12" cy="7" r="4"/><path d="M5.5 21a6.5 6.5 0 0 1 13 0"/><path d="M12 11v4"/>',
        tesoura: '<circle cx="6" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path d="M20 4 8.12 15.88M14.47 14.48 20 20M8.12 8.12 12 12"/>',
        atendimento: '<path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>',
        caixa: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2.5"/><path d="M6 12h.01M18 12h.01"/>',
        relatorios: '<path d="M3 3v18h18"/><path d="M7 16v-5M12 16V8M17 16v-8"/>',
        chave: '<circle cx="7.5" cy="15.5" r="4.5"/><path d="m10.7 12.3 9.8-9.8M17 6l3 3M15 8l2 2"/>',
        sair: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5M21 12H9"/>',
        menu: '<path d="M3 6h18M3 12h18M3 18h18"/>',
        fechar: '<path d="M18 6 6 18M6 6l12 12"/>',
        sol: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/>',
        lua: '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>',
        mais: '<path d="M12 5v14M5 12h14"/>',
        editar: '<path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>',
        lixo: '<path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6"/>',
        estrela: '<path d="m12 2 3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>',
        relogio: '<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>',
        dinheiro: '<path d="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>',
        presente: '<rect x="3" y="8" width="18" height="4" rx="1"/><path d="M12 8v13M19 12v9H5v-9M7.5 8a2.5 2.5 0 0 1 0-5C11 3 12 8 12 8s1-5 4.5-5a2.5 2.5 0 0 1 0 5"/>',
        olho: '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z"/><circle cx="12" cy="12" r="3"/>',
        baixar: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>',
        imprimir: '<path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/>',
        whatsapp: '<path d="M21 11.5a8.4 8.4 0 0 1-12.4 7.4L3 21l2.1-5.4A8.4 8.4 0 1 1 21 11.5z"/>'
    };
    const icone = (nome, rotulo) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" ${rotulo ? `role="img" aria-label="${esc(rotulo)}"` : 'aria-hidden="true" focusable="false"'}>${P[nome] || ''}</svg>`;

    // ---------- tema claro/escuro ----------
    function temaAtual() { return doc.documentElement.getAttribute('data-tema') || 'escuro'; }
    function aplicarTema(tema) {
        doc.documentElement.setAttribute('data-tema', tema);
        try { global.localStorage.setItem('vibe.tema', tema); } catch (e) { /* ignora */ }
        $$('[data-alternar-tema]').forEach(atualizarBotaoTema);
        global.dispatchEvent(new CustomEvent('vibe:tema', { detail: tema }));
    }
    function atualizarBotaoTema(btn) {
        const escuro = temaAtual() === 'escuro';
        btn.innerHTML = icone(escuro ? 'sol' : 'lua');
        btn.setAttribute('aria-label', escuro ? 'Mudar para o tema claro' : 'Mudar para o tema escuro');
        btn.setAttribute('title', btn.getAttribute('aria-label'));
        btn.setAttribute('aria-pressed', String(!escuro));
    }
    function iniciarTema() {
        $$('[data-alternar-tema]').forEach((btn) => {
            atualizarBotaoTema(btn);
            btn.addEventListener('click', () => aplicarTema(temaAtual() === 'escuro' ? 'claro' : 'escuro'));
        });
    }

    // ---------- mensagens ----------
    function toast(mensagem, tipo = 'sucesso', ms = 4200) {
        let area = $('#toasts');
        if (!area) {
            area = doc.createElement('div');
            area.id = 'toasts'; area.className = 'toasts';
            area.setAttribute('aria-live', 'polite'); area.setAttribute('role', 'status');
            doc.body.appendChild(area);
        }
        const t = doc.createElement('div');
        t.className = `toast toast-${tipo}`;
        t.innerHTML = `<div>${esc(mensagem)}</div>`;
        area.appendChild(t);
        setTimeout(() => t.remove(), ms);
    }

    /** Diálogo de confirmação acessível (substitui o JOptionPane "Sim/Não"). */
    function confirmar(titulo, mensagemHtml, { textoOk = 'Confirmar', perigo = false } = {}) {
        return new Promise((resolve) => {
            const d = doc.createElement('dialog');
            d.className = 'modal';
            d.setAttribute('aria-labelledby', 'conf-titulo');
            d.innerHTML = `<div class="modal-cabecalho"><h2 id="conf-titulo">${esc(titulo)}</h2></div>
                <div class="modal-corpo">${mensagemHtml}</div>
                <div class="modal-rodape"><button type="button" class="btn btn-secundario" value="nao">Cancelar</button>
                <button type="button" class="btn ${perigo ? 'btn-perigo' : 'btn-primario'}" value="sim">${esc(textoOk)}</button></div>`;
            doc.body.appendChild(d);
            const fim = (ok) => { d.close(); d.remove(); resolve(ok); };
            d.addEventListener('cancel', (e) => { e.preventDefault(); fim(false); });
            $$('button', d).forEach((b) => b.addEventListener('click', () => fim(b.value === 'sim')));
            d.showModal();
            $('button[value="nao"]', d).focus();
        });
    }

    // ---------- modais declarados no HTML ----------
    function abrirModal(dialogo) {
        dialogo.showModal();
        const primeiro = dialogo.querySelector('input:not([type=hidden]):not([disabled]), select, textarea');
        if (primeiro) primeiro.focus();
    }
    function ligarModais() {
        $$('dialog.modal [data-fechar]').forEach((b) => b.addEventListener('click', () => b.closest('dialog').close()));
    }

    // ---------- erros de formulário (mensagem ao lado do campo) ----------
    function limparErros(form) {
        $$('[aria-invalid="true"]', form).forEach((c) => c.removeAttribute('aria-invalid'));
        $$('.erro-campo', form).forEach((e) => { e.textContent = ''; });
        const geral = $('.erro-formulario', form);
        if (geral) geral.textContent = '';
    }
    function erroNoCampo(form, campo, mensagem) {
        const entrada = form.querySelector(`[name="${campo}"]`);
        const alvo = form.querySelector(`#erro-${campo}`) || (entrada && form.querySelector(`#erro-${entrada.id}`));
        if (entrada && alvo) {
            entrada.setAttribute('aria-invalid', 'true');
            alvo.textContent = mensagem;
            return entrada;
        }
        return null;
    }
    /** Mostra um erro vindo dos serviços: no campo certo (erro.campo) ou no topo do formulário. */
    function mostrarErro(form, erro) {
        if (!(erro instanceof Vibe.Regras.ErroValidacao || erro instanceof Vibe.Regras.ErroRegra)) {
            console.error(erro);
        }
        const entrada = erro.campo ? erroNoCampo(form, erro.campo, erro.message) : null;
        if (entrada) { entrada.focus(); return; }
        const geral = $('.erro-formulario', form);
        if (geral) { geral.textContent = erro.message; geral.focus(); } else { toast(erro.message, 'erro'); }
    }
    /** Validação "ao vivo": ao sair do campo, roda a regra e mostra/limpa o erro. */
    function validarAoSair(form, nome, regra) {
        const entrada = form.querySelector(`[name="${nome}"]`);
        if (!entrada) return;
        const rodar = () => {
            const alvo = form.querySelector(`#erro-${nome}`);
            try {
                regra(entrada.value);
                entrada.removeAttribute('aria-invalid');
                if (alvo) alvo.textContent = '';
            } catch (e) {
                if (!(e instanceof Vibe.Regras.ErroValidacao)) throw e;
                entrada.setAttribute('aria-invalid', 'true');
                if (alvo) alvo.textContent = e.message;
            }
        };
        entrada.addEventListener('blur', () => { if (entrada.value.trim() !== '' || entrada.hasAttribute('aria-invalid')) rodar(); });
        entrada.addEventListener('input', () => { if (entrada.hasAttribute('aria-invalid')) rodar(); });
    }

    // ---------- máscaras de digitação (data-mascara="telefone|cpf|data|hora") ----------
    const MASCARAS = {
        telefone: (v) => {
            const d = v.replace(/\D/g, '').slice(0, 11);
            if (d.length <= 2) return d.length ? `(${d}` : '';
            if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
            if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
            return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
        },
        cpf: (v) => {
            const d = v.replace(/\D/g, '').slice(0, 11);
            return d.replace(/^(\d{3})(\d)/, '$1.$2').replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3').replace(/\.(\d{3})(\d{1,2})$/, '.$1-$2');
        },
        data: (v) => {
            const d = v.replace(/\D/g, '').slice(0, 8);
            return d.replace(/^(\d{2})(\d)/, '$1/$2').replace(/^(\d{2})\/(\d{2})(\d)/, '$1/$2/$3');
        },
        hora: (v) => {
            const d = v.replace(/\D/g, '').slice(0, 4);
            return d.length > 2 ? `${d.slice(0, 2)}:${d.slice(2)}` : d;
        }
    };
    function aplicarMascaras(raiz = doc) {
        $$('[data-mascara]', raiz).forEach((campo) => {
            const fn = MASCARAS[campo.dataset.mascara];
            if (!fn || campo.dataset.mascaraLigada) return;
            campo.dataset.mascaraLigada = '1';
            campo.addEventListener('input', () => { campo.value = fn(campo.value); });
        });
    }

    // ---------- utilidades ----------
    function preencherSelect(select, itens, { valor = 'id', texto = 'nome', vazio = null, selecionado = null } = {}) {
        select.innerHTML = (vazio != null ? `<option value="">${esc(vazio)}</option>` : '')
            + itens.map((i) => `<option value="${esc(i[valor])}">${esc(typeof texto === 'function' ? texto(i) : i[texto])}</option>`).join('');
        if (selecionado != null) select.value = String(selecionado);
    }
    const primeiraMaiuscula = (t) => (t ? t.charAt(0).toUpperCase() + t.slice(1) : t);
    const iniciais = (nome) => String(nome || '?').split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('');
    const hojeIso = () => Vibe.Regras.isoData(new Date());
    const isoParaBr = (iso) => (iso ? Vibe.Regras.formatarData(iso) : '');

    /** Menu do site público no celular. */
    function iniciarSitePublico() {
        iniciarTema(); ligarModais(); aplicarMascaras();
        const btn = $('#btn-menu-site'); const nav = $('#site-nav');
        if (btn && nav) {
            btn.innerHTML = icone('menu');
            btn.addEventListener('click', () => {
                const aberto = nav.classList.toggle('aberto');
                btn.setAttribute('aria-expanded', String(aberto));
            });
        }
        const ano = $('#ano'); if (ano) ano.textContent = new Date().getFullYear();
    }

    /**
     * Monta a área interna: confere login/permissão, gera o menu lateral
     * apenas com as páginas do perfil e liga os botões comuns.
     */
    function iniciarAreaInterna(permissao) {
        const u = Vibe.Auth.protegerPagina(permissao);
        if (!u) return null;
        const atual = doc.body.dataset.pagina;
        $('#menu').innerHTML = Vibe.Auth.paginasDo(u).map((p) =>
            `<li><a href="${p.arquivo}"${p.arquivo === atual ? ' aria-current="page"' : ''}>${icone(p.icone)}<span>${esc(p.titulo)}</span></a></li>`).join('');
        $('#usuario-box').innerHTML = `<span class="avatar" aria-hidden="true">${esc(iniciais(u.nome))}</span>
            <span><strong>${esc(u.nome)}</strong><small>${esc(Vibe.Auth.ROTULO_PERFIL[u.perfil])}</small></span>`;
        $('#btn-sair').innerHTML = `${icone('sair')} Sair`;
        $('#btn-sair').addEventListener('click', async () => {
            if (await confirmar('Sair do sistema', '<p>Deseja realmente sair?</p>', { textoOk: 'Sair' })) {
                Vibe.Auth.sair();
                global.location.href = 'login.html';
            }
        });
        // gaveta do menu no celular/tablet
        const sidebar = $('#sidebar'); const fundo = $('#fundo-menu'); const btnMenu = $('#btn-menu');
        btnMenu.innerHTML = icone('menu');
        const alternar = (abrir) => {
            sidebar.classList.toggle('aberto', abrir); fundo.classList.toggle('aberto', abrir);
            btnMenu.setAttribute('aria-expanded', String(abrir));
            if (abrir) { const a = $('a', sidebar); if (a) a.focus(); }
        };
        btnMenu.addEventListener('click', () => alternar(!sidebar.classList.contains('aberto')));
        fundo.addEventListener('click', () => alternar(false));
        doc.addEventListener('keydown', (e) => { if (e.key === 'Escape' && sidebar.classList.contains('aberto')) { alternar(false); btnMenu.focus(); } });

        iniciarTema(); ligarModais(); aplicarMascaras();
        const aviso = global.sessionStorage.getItem('vibe.aviso');
        if (aviso) { global.sessionStorage.removeItem('vibe.aviso'); toast(aviso, 'erro'); }
        return u;
    }

    /** Executa uma ação de formulário mostrando os erros de validação/regra. */
    function tentar(form, acao) {
        limparErros(form);
        try { return acao(); }
        catch (e) { mostrarErro(form, e); return undefined; }
    }

    Vibe.UI = {
        $, $$, esc, icone, toast, confirmar, abrirModal, limparErros, mostrarErro, erroNoCampo, validarAoSair,
        aplicarMascaras, preencherSelect, iniciais, primeiraMaiuscula, hojeIso, isoParaBr, aplicarTema, temaAtual,
        iniciarSitePublico, iniciarAreaInterna, tentar
    };
})(window);
