/* Clientes (TelaClientes): pesquisa, cadastro com validação ao vivo, exclusão com regra de histórico. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(Auth.P.GERENCIAR_CLIENTES);
    if (!u) return;
    const modal = $('#modal-cliente');
    const form = $('#form-cliente');

    function render() {
        const lista = S.Clientes.listar($('#busca').value);
        $('#contagem').textContent = `${lista.length} ${lista.length === 1 ? 'cliente' : 'clientes'}`;
        $('#tabela-clientes').innerHTML = lista.length ? lista.map((c) => `
            <tr>
                <td data-label="Nome"><strong>${esc(c.nome)}</strong></td>
                <td data-label="Telefone">${esc(R.formatarTelefone(c.telefone))}</td>
                <td data-label="E-mail">${esc(c.email || '—')}</td>
                <td data-label="Pontos" class="num"><span class="badge badge-dourado">${c.pontos} pts</span></td>
                <td data-label="Último Corte">${c.ultimoCorte ? R.formatarData(c.ultimoCorte) : '—'}</td>
                <td class="acoes">
                    <button type="button" class="btn btn-texto btn-peq" data-hist="${c.id}">Histórico</button>
                    <button type="button" class="btn btn-secundario btn-peq" data-editar="${c.id}" aria-label="Editar ${esc(c.nome)}">Editar</button>
                    <button type="button" class="btn btn-perigo btn-peq" data-excluir="${c.id}" aria-label="Excluir ${esc(c.nome)}">Excluir</button>
                </td>
            </tr>`).join('') : '<tr><td colspan="6" class="vazio">Nenhum cliente encontrado.</td></tr>';
        $$('[data-editar]').forEach((b) => b.addEventListener('click', () => abrir(window.Vibe.Dados.obter('clientes', Number(b.dataset.editar)))));
        $$('[data-excluir]').forEach((b) => b.addEventListener('click', () => excluir(Number(b.dataset.excluir))));
        $$('[data-hist]').forEach((b) => b.addEventListener('click', () => historico(Number(b.dataset.hist))));
    }

    function abrir(c) {
        form.reset(); UI.limparErros(form);
        $('#modal-cli-titulo').textContent = c ? 'Editar Cliente' : 'Cadastrar Novo Cliente';
        form.id.value = c ? c.id : '';
        form.nome.value = c ? c.nome : '';
        form.telefone.value = c ? R.formatarTelefone(c.telefone) : '';
        form.email.value = c && c.email ? c.email : '';
        form.cpf.value = c && c.cpf ? R.formatarCpf(c.cpf) : '';
        UI.abrirModal(modal);
    }
    UI.validarAoSair(form, 'nome', (v) => R.obrigatorio(v, 'Nome é obrigatório.', 'nome'));
    UI.validarAoSair(form, 'telefone', (v) => R.telefone(v));
    UI.validarAoSair(form, 'email', (v) => R.emailOpcional(v));
    UI.validarAoSair(form, 'cpf', (v) => (R.vazio(v) ? null : R.cpf(v)));

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = Number(form.id.value) || null;
        const ok = UI.tentar(form, () => S.Clientes.salvar(u, { id, nome: form.nome.value, telefone: form.telefone.value, email: form.email.value, cpf: form.cpf.value }));
        if (!ok) return;
        modal.close();
        UI.toast(id ? 'Cliente atualizado com sucesso!' : 'Cliente cadastrado com sucesso!');
        render();
    });

    async function excluir(id) {
        const c = window.Vibe.Dados.obter('clientes', id);
        if (!await UI.confirmar('Confirmar Exclusão', `<p>Deseja realmente excluir o cliente <strong>${esc(c.nome)}</strong>?</p>`, { textoOk: 'Excluir', perigo: true })) return;
        try { S.Clientes.excluir(u, id); UI.toast('Cliente excluído com sucesso!'); render(); }
        catch (err) { UI.toast(err.message, 'erro', 6000); }
    }

    function historico(id) {
        const c = window.Vibe.Dados.obter('clientes', id);
        const movs = S.Clientes.historico(id);
        const total = movs.reduce((s, m) => s + m.valor, 0);
        $('#modal-hist-titulo').textContent = `Histórico — ${c.nome}`;
        $('#historico-corpo').innerHTML = `<p><span class="badge badge-dourado">${c.pontos} pontos</span> · ${movs.length} visitas · ${R.formatarMoeda(total)} gastos</p>`
            + (movs.length ? `<ul class="lista-historico">${movs.slice(0, 12).map((m) => `<li><span>${R.formatarData(m.dataHora)}</span><span>${esc(m.descricao)}</span>
                <strong>${R.formatarMoeda(m.valor)}</strong><small>+${m.pontosGanhos} pts${m.pontosResgatados ? ` · −${m.pontosResgatados} resgatados` : ''}</small></li>`).join('')}</ul>`
                : '<p class="texto-secundario">Ainda sem atendimentos.</p>');
        UI.abrirModal($('#modal-historico'));
    }

    $('#busca').addEventListener('input', render);
    $('#btn-novo').addEventListener('click', () => abrir(null));
    render();
    if (new URLSearchParams(location.search).get('novo')) abrir(null);
})();
