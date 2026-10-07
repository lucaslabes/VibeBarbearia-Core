/* Alterar senha (TelaAlterarSenha + PoliticaSenha) com indicador de força. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R } = window.Vibe;
    const { $, esc } = UI;
    const u = UI.iniciarAreaInterna(null);
    if (!u) return;
    const form = $('#form-senha');
    $('#usuario-senha').innerHTML = `Usuário: <strong>${esc(u.login)}</strong> (${esc(Auth.ROTULO_PERFIL[u.perfil])})`;
    const niveis = { 'Muito fraca': 10, 'Fraca': 30, 'Média': 55, 'Boa': 80, 'Forte': 100 };
    form.nova.addEventListener('input', () => {
        const f = R.forcaSenha(form.nova.value);
        $('#forca-barra').style.width = form.nova.value ? `${niveis[f]}%` : '0%';
        $('#forca-texto').textContent = form.nova.value ? `Força da senha: ${f}` : 'Força da senha';
    });
    UI.validarAoSair(form, 'confirmacao', (v) => { if (v && v !== form.nova.value) throw new R.ErroValidacao('A confirmação não confere com a nova senha.', 'confirmacao'); });
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const ok = UI.tentar(form, () => { Auth.alterarSenha(form.atual.value, form.nova.value, form.confirmacao.value); return true; });
        if (!ok) return;
        form.reset();
        $('#forca-barra').style.width = '0%';
        UI.toast('Senha alterada com sucesso.');
    });
})();
