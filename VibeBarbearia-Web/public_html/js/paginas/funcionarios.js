/* Funcionários (TelaBarbeiros): barbeiros e gerentes com as regras de permissão do dono e do gerente. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Dados: D } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(Auth.P.VISUALIZAR_FUNCIONARIOS);
    if (!u) return;
    const dono = Auth.pode(u, Auth.P.CADASTRAR_FUNCIONARIO);
    const modal = $('#modal-funcionario');
    const form = $('#form-funcionario');
    $('#btn-novo').hidden = !dono;
    $('#aviso-gerente').hidden = dono;

    function render() {
        const perfil = $('#filtro-perfil').value;
        const lista = S.Funcionarios.listar($('#busca').value).filter((f) => !perfil || f.perfil === perfil);
        $('#cards-equipe').innerHTML = lista.length ? lista.map((f) => {
            const chave = `${f.perfil}:${f.id}`;
            const podeEditar = f.perfil === 'BARBEIRO' || Auth.pode(u, Auth.P.GERENCIAR_GERENTES);
            const podeRemover = dono && !(f.perfil === 'GERENTE' && f.id === u.id);
            return `<article class="card func-card${f.ativo ? '' : ' inativo'}">
                <div class="linha"><span class="avatar" aria-hidden="true">${esc(UI.iniciais(f.nome))}</span>
                    <div><h2 class="h3">${esc(f.nome)}</h2>
                    <span class="badge ${f.perfil === 'GERENTE' ? 'badge-online' : 'badge-dourado'}">${f.perfil === 'GERENTE' ? 'Gerente' : 'Barbeiro'}</span>
                    ${f.ativo ? '' : '<span class="badge badge-inativo">Inativo</span>'}</div></div>
                <dl class="func-dados">
                    <dt>CPF (login)</dt><dd>${esc(R.formatarCpf(f.cpf || ''))}</dd>
                    <dt>Telefone</dt><dd>${esc(R.formatarTelefone(f.telefone || ''))}</dd>
                    <dt>E-mail</dt><dd>${esc(f.email || '—')}</dd>
                    ${f.perfil === 'BARBEIRO' ? `<dt>Comissão</dt><dd><strong class="texto-dourado">${f.comissao}%</strong></dd><dt>Especialidade</dt><dd>${esc(f.especialidade || '—')}</dd>` : ''}
                </dl>
                <div class="linha">
                    ${podeEditar ? `<button type="button" class="btn btn-secundario btn-peq" data-editar="${chave}">Editar</button>` : ''}
                    ${podeRemover ? `<button type="button" class="btn btn-perigo btn-peq" data-remover="${chave}">${f.perfil === 'GERENTE' ? 'Demitir' : 'Remover'}</button>` : ''}
                </div>
            </article>`;
        }).join('') : '<p class="texto-secundario">Nenhum funcionário encontrado.</p>';
        $$('[data-editar]').forEach((b) => b.addEventListener('click', () => abrir(b.dataset.editar)));
        $$('[data-remover]').forEach((b) => b.addEventListener('click', () => remover(b.dataset.remover)));
    }

    function ajustarPerfil() {
        const barbeiro = form.perfil.value === 'BARBEIRO';
        $$('.so-barbeiro', form).forEach((el) => { el.hidden = !barbeiro; });
        const podeComissao = Auth.pode(u, Auth.P.ALTERAR_COMISSAO);
        form.comissao.readOnly = !podeComissao;
        form.comissao.previousElementSibling.innerHTML = podeComissao ? 'Comissão (%) <span class="obrigatorio" aria-hidden="true">*</span>' : 'Comissão (%) (somente leitura)';
    }
    function abrir(chave) {
        form.reset(); UI.limparErros(form);
        const [perfil, idTxt] = chave ? chave.split(':') : ['BARBEIRO', ''];
        const id = Number(idTxt) || null;
        const f = id ? (perfil === 'GERENTE' ? D.obter('usuarios', id) : D.obter('barbeiros', id)) : null;
        $('#modal-func-titulo').textContent = f ? `Editar ${perfil === 'GERENTE' ? 'gerente' : 'barbeiro'}` : 'Cadastrar funcionário';
        form.id.value = id || '';
        $$('input[name="perfil"]', form).forEach((r) => { r.checked = r.value === perfil; r.disabled = !!f; });
        $('#grupo-perfil').hidden = !!f;
        $('#aviso-senha-inicial').hidden = !!f;
        if (f) {
            form.nome.value = f.nome; form.cpf.value = R.formatarCpf(f.cpf || ''); form.telefone.value = R.formatarTelefone(f.telefone || '');
            form.email.value = f.email || '';
            if (perfil === 'BARBEIRO') { form.comissao.value = f.comissao; form.especialidade.value = f.especialidade || ''; form.ativo.checked = f.ativo; }
        } else { form.comissao.value = R.CONST.COMISSAO_PADRAO_PERCENT; }
        ajustarPerfil();
        UI.abrirModal(modal);
    }
    $$('input[name="perfil"]', form).forEach((r) => r.addEventListener('change', ajustarPerfil));
    UI.validarAoSair(form, 'nome', (v) => R.obrigatorio(v, 'Nome é obrigatório.', 'nome'));
    UI.validarAoSair(form, 'cpf', (v) => R.cpf(v, { exigirDigitoVerificador: !form.id.value }));
    UI.validarAoSair(form, 'telefone', (v) => R.telefone(v));
    UI.validarAoSair(form, 'email', (v) => R.emailObrigatorio(v));
    UI.validarAoSair(form, 'comissao', (v) => R.comissaoPercentual(v));

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = Number(form.id.value) || null;
        const dados = { id, perfil: form.perfil.value, nome: form.nome.value, cpf: form.cpf.value, telefone: form.telefone.value,
            email: form.email.value, comissao: form.comissao.value, especialidade: form.especialidade.value, ativo: form.ativo.checked };
        const ok = UI.tentar(form, () => S.Funcionarios.salvar(u, dados));
        if (!ok) return;
        modal.close();
        UI.toast(id ? 'Dados atualizados com sucesso.' : `${dados.nome.trim()} cadastrado com sucesso. Login: CPF · senha inicial 123.`);
        render();
    });

    async function remover(chave) {
        const [perfil, idTxt] = chave.split(':');
        const id = Number(idTxt);
        try {
            if (perfil === 'GERENTE') {
                const g = D.obter('usuarios', id);
                if (!await UI.confirmar('Confirmar demissão', `<p>Deseja demitir o gerente <strong>${esc(g.nome)}</strong>? O login dele será removido.</p>`, { textoOk: 'Demitir', perigo: true })) return;
                S.Funcionarios.demitirGerente(u, id);
                UI.toast('Gerente demitido.');
            } else {
                const b = D.obter('barbeiros', id);
                if (!await UI.confirmar('Confirmar Exclusão', `<p>Deseja remover <strong>${esc(b.nome)}</strong> da equipe?</p><p class="texto-secundario">O login será removido. Se houver agenda ou caixa no histórico, o cadastro fica inativo para preservar os relatórios.</p>`, { textoOk: 'Remover', perigo: true })) return;
                const r = S.Funcionarios.removerBarbeiro(u, id);
                UI.toast(r === 'INATIVADO' ? `${b.nome} foi inativado (possui histórico) e o login removido.` : 'Barbeiro e login excluídos com sucesso.');
            }
            render();
        } catch (err) { UI.toast(err.message, 'erro'); }
    }

    $('#busca').addEventListener('input', render);
    $('#filtro-perfil').addEventListener('change', render);
    $('#btn-novo').addEventListener('click', () => abrir(null));
    render();
})();
