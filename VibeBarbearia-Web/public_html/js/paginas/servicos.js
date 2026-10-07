/* Catálogo de serviços e produtos (Gerenciar catálogo da TelaRegistroAtendimento) — somente o dono. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Dados: D } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(Auth.P.GERENCIAR_CATALOGO);
    if (!u) return;
    const modal = $('#modal-item');
    const form = $('#form-item');
    let tipo = '';

    function render() {
        const lista = S.Catalogo.listar(tipo);
        $('#grade-catalogo').innerHTML = lista.length ? lista.map((s) => `
            <article class="card item-card">
                <div class="linha-entre"><span class="badge ${s.tipo === 'PRODUTO' ? 'badge-online' : 'badge-dourado'}">${s.tipo === 'PRODUTO' ? 'Produto' : 'Serviço'}</span>
                <span class="preco">${R.formatarMoeda(s.preco)}</span></div>
                <h2 class="h3">${esc(s.nome)}</h2>
                <p class="texto-secundario">${esc(s.descricao || '')}</p>
                <div class="linha">
                    <button type="button" class="btn btn-secundario btn-peq" data-editar="${s.id}">Editar</button>
                    <button type="button" class="btn btn-perigo btn-peq" data-excluir="${s.id}">Excluir</button>
                </div>
            </article>`).join('') : '<p class="texto-secundario">Nenhum item no catálogo. Clique em “Novo serviço / produto” para cadastrar.</p>';
        $$('[data-editar]').forEach((b) => b.addEventListener('click', () => abrir(D.obter('servicos', Number(b.dataset.editar)))));
        $$('[data-excluir]').forEach((b) => b.addEventListener('click', () => excluir(Number(b.dataset.excluir))));
    }
    function abrir(s) {
        form.reset(); UI.limparErros(form);
        $('#modal-item-titulo').textContent = s ? 'Editar item do catálogo' : 'Novo serviço / produto';
        form.id.value = s ? s.id : '';
        form.nome.value = s ? s.nome : '';
        form.tipo.value = s ? s.tipo : (tipo || 'SERVICO');
        form.preco.value = s ? s.preco.toFixed(2).replace('.', ',') : '';
        form.descricao.value = s ? s.descricao || '' : '';
        UI.abrirModal(modal);
    }
    UI.validarAoSair(form, 'nome', (v) => R.obrigatorio(v, 'Informe o nome do item.', 'nome'));
    UI.validarAoSair(form, 'preco', (v) => R.precoNaoNegativo(v));
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = Number(form.id.value) || null;
        const ok = UI.tentar(form, () => S.Catalogo.salvar(u, { id, nome: form.nome.value, tipo: form.tipo.value, preco: form.preco.value, descricao: form.descricao.value }));
        if (!ok) return;
        modal.close();
        UI.toast(id ? 'Item atualizado no catálogo.' : 'Item cadastrado e disponível no atendimento.');
        render();
    });
    async function excluir(id) {
        const s = D.obter('servicos', id);
        if (!await UI.confirmar('Excluir item', `<p>Remover <strong>${esc(s.nome)}</strong> do catálogo? O histórico do caixa não é alterado.</p>`, { textoOk: 'Excluir', perigo: true })) return;
        S.Catalogo.excluir(u, id); UI.toast('Item removido do catálogo.'); render();
    }
    $$('.aba').forEach((a) => a.addEventListener('click', () => {
        tipo = a.dataset.tipo;
        $$('.aba').forEach((x) => x.setAttribute('aria-selected', String(x === a)));
        render();
    }));
    $('#btn-novo').addEventListener('click', () => abrir(null));
    render();
})();
