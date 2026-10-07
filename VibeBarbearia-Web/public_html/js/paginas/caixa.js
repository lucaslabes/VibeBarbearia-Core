/* Caixa (TelaCaixa): filtros, totais (ResumoCaixa) e exportação CSV. Barbeiro vê só os próprios movimentos. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Dados: D } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(null);
    if (!u) return;
    const completo = Auth.pode(u, Auth.P.VER_CAIXA_COMPLETO);
    const form = $('#form-filtros');
    let atuais = [];
    $('#aviso-barbeiro').hidden = completo;
    UI.preencherSelect(form.cliente, S.Clientes.listar(), { vazio: 'Todos os clientes' });
    UI.preencherSelect(form.barbeiro, D.listar('barbeiros'), { vazio: 'Todos os barbeiros' });
    if (!completo) form.barbeiro.closest('.campo').hidden = true;

    function definirPeriodo(tipo) {
        const hoje = new Date();
        const ini = tipo === 'mes' ? new Date(hoje.getFullYear(), hoje.getMonth(), 1) : hoje;
        form.inicio.value = R.formatarData(R.isoData(ini));
        form.fim.value = R.formatarData(R.isoData(hoje));
        aplicar();
    }
    function aplicar() {
        UI.limparErros(form);
        const filtros = UI.tentar(form, () => ({
            inicio: R.vazio(form.inicio.value) ? null : R.data(form.inicio.value, 'inicio'),
            fim: R.vazio(form.fim.value) ? null : R.data(form.fim.value, 'fim'),
            idCliente: Number(form.cliente.value) || null, idBarbeiro: Number(form.barbeiro.value) || null,
            forma: form.forma.value || null, texto: form.texto.value
        }));
        if (!filtros) return;
        const movs = UI.tentar(form, () => S.Caixa.filtrar(u, filtros));
        if (!movs) return;
        atuais = movs;
        const r = S.Caixa.resumo(movs);
        $('#cx-kpis').innerHTML = [
            ['Total do Período', R.formatarMoeda(r.total), `${r.quantidade} movimentos`],
            [completo ? 'Comissões' : 'Minhas comissões', R.formatarMoeda(r.totalComissao), 'Repasse aos barbeiros'],
            [completo ? 'Saldo da barbearia' : 'Ticket médio', completo ? R.formatarMoeda(r.saldoBarbearia) : R.formatarMoeda(r.ticketMedio), completo ? 'Total − comissões' : 'Por atendimento'],
            ['Por forma', `PIX ${R.formatarMoeda(r.porForma.PIX)}`, `Cartão ${R.formatarMoeda(r.porForma.CARTAO)} · Dinheiro ${R.formatarMoeda(r.porForma.DINHEIRO)}`]
        ].map(([t, v, d]) => `<article class="kpi"><div class="kpi-topo"><span>${esc(t)}</span></div><p class="kpi-valor kpi-menor">${esc(v)}</p><p class="kpi-rodape">${esc(d)}</p></article>`).join('');
        $('#cx-contagem').textContent = `(${movs.length} ${movs.length === 1 ? 'movimento' : 'movimentos'})`;
        $('#tabela-caixa').innerHTML = movs.length ? movs.slice(0, 200).map((m) => `
            <tr>
                <td data-label="Data">${R.formatarData(m.dataHora)}</td><td data-label="Hora">${R.formatarHora(m.dataHora)}</td>
                <td data-label="Cliente">${esc(D.nomeCliente(m.idCliente))}</td><td data-label="Barbeiro">${esc(D.nomeBarbeiro(m.idBarbeiro))}</td>
                <td data-label="Serviços">${esc(m.descricao)}${m.desconto ? ' <span class="badge badge-dourado" title="Desconto por pontos">−pts</span>' : ''}</td>
                <td data-label="Pagamento">${S.Caixa.FORMAS[m.formaPagamento]}</td>
                <td data-label="Valor" class="num">${R.formatarMoeda(m.valor)}</td><td data-label="Comissão" class="num">${R.formatarMoeda(m.comissao)}</td>
            </tr>`).join('') : '<tr><td colspan="8" class="vazio">Nenhum movimento encontrado</td></tr>';
    }
    function exportarCsv() {
        const linhas = [['Data', 'Hora', 'Cliente', 'Barbeiro', 'Serviços', 'Pagamento', 'Valor bruto', 'Desconto', 'Valor', 'Comissão']]
            .concat(atuais.map((m) => [R.formatarData(m.dataHora), R.formatarHora(m.dataHora), D.nomeCliente(m.idCliente), D.nomeBarbeiro(m.idBarbeiro),
                m.descricao, S.Caixa.FORMAS[m.formaPagamento], m.valorBruto, m.desconto, m.valor, m.comissao]
                .map((v) => (typeof v === 'number' ? v.toFixed(2).replace('.', ',') : v))));
        const csv = '\ufeff' + linhas.map((l) => l.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(';')).join('\r\n');
        const a = document.createElement('a');
        a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
        a.download = `caixa-vibe-${UI.hojeIso()}.csv`;
        document.body.appendChild(a); a.click(); a.remove();
        UI.toast(`${atuais.length} movimentos exportados.`);
    }
    form.addEventListener('submit', (e) => { e.preventDefault(); aplicar(); });
    $$('[data-periodo]').forEach((b) => b.addEventListener('click', () => definirPeriodo(b.dataset.periodo)));
    $('#btn-limpar').addEventListener('click', () => { form.reset(); aplicar(); });
    $('#btn-csv').addEventListener('click', exportarCsv);
    form.texto.addEventListener('input', aplicar);
    definirPeriodo('mes');
})();
