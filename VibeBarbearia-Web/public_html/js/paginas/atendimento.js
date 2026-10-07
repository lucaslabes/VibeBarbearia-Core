/* Registro de Atendimento: resumo ao vivo com comissão, pontos e desconto por fidelidade (regras do Core). */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Dados: D } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(Auth.P.REGISTRAR_ATENDIMENTO);
    if (!u) return;
    const form = $('#form-atendimento');
    const selecionados = new Set();

    function carregarAgendamentos(sel) {
        const pend = S.Agenda.pendentes(u);
        UI.preencherSelect(form.agendamento, pend, {
            vazio: pend.length ? 'Selecione o agendamento…' : 'Não há agendamentos pendentes. Crie um na Agenda.',
            texto: (a) => `${R.formatarData(a.dataHora)} ${R.formatarHora(a.dataHora)} — ${D.nomeCliente(a.idCliente)} — ${D.nomeBarbeiro(a.idBarbeiro)}`,
            selecionado: sel
        });
    }
    function renderItens() {
        const filtro = $('#at-busca').value.trim().toLowerCase();
        const grupos = { SERVICO: 'Serviços', PRODUTO: 'Produtos' };
        $('#lista-itens').innerHTML = Object.keys(grupos).map((t) => {
            const itens = S.Catalogo.listar(t).filter((s) => !filtro || s.nome.toLowerCase().includes(filtro));
            if (!itens.length) return '';
            return `<p class="rotulo">${grupos[t]}</p>` + itens.map((s) => `
                <label class="item-linha"><input type="checkbox" value="${s.id}"${selecionados.has(s.id) ? ' checked' : ''}>
                <span>${esc(s.nome)}</span><strong>${R.formatarMoeda(s.preco)}</strong></label>`).join('');
        }).join('') || '<p class="texto-secundario">Nenhum item encontrado.</p>';
        $$('#lista-itens input').forEach((c) => c.addEventListener('change', () => {
            c.checked ? selecionados.add(Number(c.value)) : selecionados.delete(Number(c.value));
            $('#erro-itens').textContent = '';
            atualizar();
        }));
    }

    function atualizar() {
        const id = Number(form.agendamento.value);
        const info = $('#cliente-info');
        if (!id) {
            info.innerHTML = '';
            $('#recibo-itens').innerHTML = '<li class="texto-secundario">Selecione um agendamento.</li>';
            ['rc-bruto', 'rc-desconto', 'rc-pago', 'rc-comissao'].forEach((x) => { $('#' + x).textContent = R.formatarMoeda(0); });
            $('#rc-pontos').textContent = '0'; $('#rc-saldo').textContent = '—';
            return;
        }
        const s = S.Atendimento.simular(id, [...selecionados], $('#at-resgatar').checked);
        info.innerHTML = `<span class="avatar" aria-hidden="true">${esc(UI.iniciais(s.cliente.nome))}</span>
            <span><strong>${esc(s.cliente.nome)}</strong><small>${esc(R.formatarTelefone(s.cliente.telefone))} · <span class="texto-dourado">${s.cliente.pontos} pontos</span></small></span>
            <span class="cliente-barbeiro">Barbeiro: <strong>${esc(s.barbeiro.nome)}</strong> (${s.barbeiro.comissao}%)</span>`;
        // resgate só fica disponível se a regra permitir algum bloco
        const chk = $('#at-resgatar');
        const possivel = s.resgatePossivel.pontosUsados > 0;
        chk.disabled = !possivel;
        if (!possivel) chk.checked = false;
        $('#resgate-ajuda').textContent = possivel
            ? `Pode usar ${s.resgatePossivel.pontosUsados} pontos = ${R.formatarMoeda(s.resgatePossivel.valorDesconto)} de desconto (máx. 50% do valor).`
            : s.cliente.pontos < R.CONST.PONTOS_POR_RESGATE
                ? `O cliente tem ${s.cliente.pontos} pontos: são necessários ${R.CONST.PONTOS_POR_RESGATE} para resgatar.`
                : 'Selecione os itens: o desconto depende do valor (máx. 50%).';
        const c = S.Atendimento.simular(id, [...selecionados], chk.checked).calc;
        $('#recibo-itens').innerHTML = s.itens.length ? s.itens.map((i) => `<li><span>${esc(i.nome)}</span><span>${R.formatarMoeda(i.preco)}</span></li>`).join('')
            : '<li class="texto-secundario">Nenhum item selecionado.</li>';
        $('#rc-bruto').textContent = R.formatarMoeda(c.bruto);
        $('#rc-desconto').textContent = c.desconto ? `− ${R.formatarMoeda(c.desconto)} (${c.pontosResgatados} pts)` : R.formatarMoeda(0);
        $('#rc-pago').textContent = R.formatarMoeda(c.pago);
        $('#rc-comissao-rotulo').textContent = `Comissão do barbeiro (${s.barbeiro.comissao}%)`;
        $('#rc-comissao').textContent = R.formatarMoeda(c.comissao);
        $('#rc-pontos').textContent = `+${c.pontosGanhos}`;
        $('#rc-saldo').textContent = `${c.pontosFinais} pontos`;
    }

    form.agendamento.addEventListener('change', () => { $('#erro-agendamento').textContent = ''; form.agendamento.removeAttribute('aria-invalid'); atualizar(); });
    $('#at-resgatar').addEventListener('change', atualizar);
    $('#at-busca').addEventListener('input', renderItens);
    $$('input[name="pagamento"]').forEach((r) => r.addEventListener('change', () => { $('#erro-pagamento').textContent = ''; }));

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const forma = (form.querySelector('input[name="pagamento"]:checked') || {}).value;
        const r = UI.tentar(form, () => S.Atendimento.finalizar(u, {
            idAgendamento: Number(form.agendamento.value) || null, idsItens: [...selecionados], formaPagamento: forma, resgatar: $('#at-resgatar').checked
        }));
        if (!r) {
            // erros de grupos (itens/pagamento) não têm <input> com o nome: mostra no parágrafo do grupo
            const geral = form.querySelector('.erro-formulario');
            if (/item/.test(geral.textContent)) { $('#erro-itens').textContent = geral.textContent; geral.textContent = ''; }
            if (/pagamento/.test(geral.textContent)) { $('#erro-pagamento').textContent = geral.textContent; geral.textContent = ''; }
            return;
        }
        const m = r.movimento;
        $('#concluido-corpo').innerHTML = `<p class="alerta alerta-sucesso">Atendimento finalizado com sucesso!</p>
            <div class="recibo" id="recibo-impressao"><dl>
                <dt>Cliente</dt><dd>${esc(r.cliente.nome)}</dd>
                <dt>Barbeiro</dt><dd>${esc(r.barbeiro.nome)}</dd>
                <dt>Itens</dt><dd>${esc(m.descricao)}</dd>
                <dt>Pagamento</dt><dd>${S.Caixa.FORMAS[m.formaPagamento]}</dd>
                <dt>Subtotal</dt><dd>${R.formatarMoeda(m.valorBruto)}</dd>
                <dt>Desconto fidelidade</dt><dd>${m.desconto ? `− ${R.formatarMoeda(m.desconto)} (${m.pontosResgatados} pts)` : '—'}</dd>
                <hr><dt>Total pago</dt><dd class="total">${R.formatarMoeda(m.valor)}</dd>
                <dt>Comissão barbeiro</dt><dd>${R.formatarMoeda(m.comissao)}</dd>
                <dt>Pontos ganhos</dt><dd>+${m.pontosGanhos}</dd>
                <dt>Total de pontos</dt><dd>${r.calc.pontosFinais}</dd>
                <dt>Status do agendamento</dt><dd><span class="badge badge-concluido">Concluído</span></dd>
            </dl></div>`;
        UI.abrirModal($('#modal-concluido'));
        selecionados.clear(); form.reset();
        carregarAgendamentos(); renderItens(); atualizar();
    });
    $('#btn-imprimir').addEventListener('click', () => window.print());

    carregarAgendamentos(new URLSearchParams(location.search).get('agendamento'));
    renderItens();
    atualizar();
})();
