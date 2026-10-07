/* Recuperação de senha (TelaRecuperarSenha) em modo demonstração: sem servidor de e-mail. */
(function () {
    'use strict';
    const { UI, Auth } = window.Vibe;
    const { $, esc } = UI;
    const form = $('#form-recuperar');
    UI.iniciarSitePublico();

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const saida = $('#rec-resultado');
        saida.innerHTML = '';
        const r = UI.tentar(form, () => Auth.recuperarSenha(form.login.value));
        if (!r) return;
        if (!r.encontrado) {
            saida.innerHTML = '<p class="alerta">Se o login existir e tiver e-mail cadastrado, uma senha provisória será enviada.</p>';
        } else if (r.semEmail) {
            saida.innerHTML = '<p class="alerta">Este usuário não possui e-mail cadastrado. Peça ao <strong>dono</strong> para atualizar o cadastro ou entre em contato com o administrador.</p>';
        } else {
            saida.innerHTML = `<div class="alerta alerta-dourado">
                <h2 class="h3">Senha provisória gerada</h2>
                <p class="texto-secundario">Modo demonstração: sem servidor de e-mail, a senha é exibida aqui. No sistema real ela seria enviada para <strong>${esc(r.emailMascarado)}</strong>.</p>
                <p>Login: <strong>${esc(r.login)}</strong><br>Senha provisória: <strong class="mono senha-provisoria">${esc(r.senha)}</strong></p>
                <p>Entre com o login e a senha provisória. Depois use <strong>Alterar senha</strong> no sistema.</p>
                <a class="btn btn-primario" href="login.html">Ir para o login</a></div>`;
        }
    });
})();
