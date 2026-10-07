/* Login simulado (TelaLogin): mensagem genérica, acessos de demonstração e restauração dos dados. */
(function () {
    'use strict';
    const { UI, Auth, Dados } = window.Vibe;
    const { $, $$ } = UI;
    const form = $('#form-login');

    // mostrar/ocultar senha
    $$('.mostrar-senha').forEach((b) => {
        const alvo = document.getElementById(b.dataset.alvo);
        b.innerHTML = UI.icone('olho');
        b.addEventListener('click', () => {
            const mostrar = alvo.type === 'password';
            alvo.type = mostrar ? 'text' : 'password';
            b.setAttribute('aria-pressed', String(mostrar));
            b.setAttribute('aria-label', mostrar ? 'Ocultar senha' : 'Mostrar senha');
        });
    });

    $$('.demo-item').forEach((b) => b.addEventListener('click', () => {
        form.login.value = b.dataset.login;
        form.senha.value = b.dataset.senha;
        UI.limparErros(form);
        form.querySelector('button[type=submit]').focus();
    }));

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        UI.limparErros(form);
        if (!form.login.value.trim()) UI.erroNoCampo(form, 'login', 'Informe o usuário.');
        if (!form.senha.value) UI.erroNoCampo(form, 'senha', 'Informe a senha.');
        if (form.querySelector('[aria-invalid="true"]')) { form.querySelector('[aria-invalid="true"]').focus(); return; }
        const u = UI.tentar(form, () => Auth.entrar(form.login.value, form.senha.value));
        if (!u) { form.senha.value = ''; return; }
        const voltar = new URLSearchParams(location.search).get('voltar');
        location.href = voltar && /^[a-z-]+\.html$/.test(voltar) ? voltar : 'dashboard.html';
    });

    $('#btn-resetar').addEventListener('click', async () => {
        if (await UI.confirmar('Restaurar dados', '<p>Todos os cadastros, agendamentos e movimentos feitos neste navegador serão apagados e os dados de demonstração recriados.</p>', { textoOk: 'Restaurar', perigo: true })) {
            Dados.resetar(); Auth.sair();
            UI.toast('Dados de demonstração restaurados.');
        }
    });

    const logado = Auth.usuario();
    if (logado) UI.toast(`Você já está conectado como ${logado.nome}. Entre de novo ou acesse o dashboard.`, 'info', 6000);
    UI.iniciarSitePublico();
})();
