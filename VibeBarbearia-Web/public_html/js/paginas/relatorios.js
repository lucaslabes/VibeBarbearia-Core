/* Relatórios (TelaRelatorios): 6 relatórios com gráficos em canvas, CSV e impressão. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Dados: D, Graficos: G } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(Auth.P.VER_RELATORIOS);
    if (!u) return;
    const form = $('#form-periodo');
    const painel = $('#painel-relatorio');
    let rel = 'resumo';
    let periodo = null;
    let tabelaAtual = [];

    function definir(tipo) {
        const hoje = new Date();
        let ini = hoje;
        if (tipo === 'mes') ini = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
        if (tipo === '30') { ini = new Date(hoje); ini.setDate(ini.getDate() - 29); }
        form.inicio.value = R.formatarData(R.isoData(ini));
        form.fim.value = R.formatarData(R.isoData(hoje));
        gerar();
    }
    function gerar() {
        const p = UI.tentar(form, () => R.periodo(R.data(form.inicio.value, 'inicio'), R.data(form.fim.value, 'fim')));
        if (!p) return;
        periodo = p;
        render();
    }
    const tabela = (cab, linhas, numericas = []) => {
        tabelaAtual = [cab, ...linhas];
        return `<div class="tabela-container"><table class="tabela tabela-responsiva"><caption class="visualmente-oculto">Dados do relatório selecionado</caption><thead><tr>${cab.map((c, i) => `<th scope="col"${numericas.includes(i) ? ' class="num"' : ''}>${esc(c)}</th>`).join('')}</tr></thead>
        <tbody>${linhas.length ? linhas.map((l) => `<tr>${l.map((v, i) => `<td data-label="${esc(cab[i])}"${numericas.includes(i) ? ' class="num"' : ''}>${esc(v)}</td>`).join('')}</tr>`).join('')
            : `<tr><td colspan="${cab.length}" class="vazio">Nenhum dado no período</td></tr>`}</tbody></table></div>`;
    };
    const kpis = (lista) => `<div class="grade-4">${lista.map(([t, v]) => `<article class="kpi"><div class="kpi-topo"><span>${esc(t)}</span></div><p class="kpi-valor kpi-menor">${esc(v)}</p></article>`).join('')}</div>`;
    const M = R.formatarMoeda;

    function render() {
        const movs = S.Caixa.filtrar(u, { inicio: periodo.inicio, fim: periodo.fim });
        const r = S.Caixa.resumo(movs);
        const titulo = `${$(`[data-rel="${rel}"]`).textContent} · ${R.formatarData(periodo.inicio)} → ${R.formatarData(periodo.fim)}`;
        let html = `<h2 class="titulo-relatorio">${esc(titulo)}</h2>`;
        painel.setAttribute('aria-labelledby', `aba-${rel}`);
        if (rel === 'resumo') {
            const dias = Math.round((R.paraDate(periodo.fim) - R.paraDate(periodo.inicio)) / 86400000) + 1;
            html += kpis([['Faturamento total', M(r.total)], ['Atendimentos (caixa)', r.quantidade], ['Ticket médio', M(r.ticketMedio)], ['Comissões totais', M(r.totalComissao)]])
                + `<div class="card"><h3>Faturamento por dia</h3><canvas id="g1" data-altura="260"></canvas></div>`
                + tabela(['Indicador', 'Valor'], [['Faturamento total', M(r.total)], ['Saldo da barbearia (total − comissões)', M(r.saldoBarbearia)],
                    ['Descontos de fidelidade concedidos', M(r.descontos)], ['Clientes atendidos', new Set(movs.map((m) => m.idCliente)).size],
                    ['Pontos fidelidade distribuídos', movs.reduce((s, m) => s + m.pontosGanhos, 0)], ['Média por dia', M(r.total / dias)]], [1]);
            painel.innerHTML = html;
            G.barras($('#g1'), S.Indicadores.seriePorDia(movs, Math.min(dias, 31), R.paraDate(periodo.fim)), { descricao: `Faturamento diário no período. Total ${M(r.total)}.` });
        } else if (rel === 'barbeiros') {
            const lista = S.Indicadores.porBarbeiro(movs);
            html += lista.length ? `<p class="alerta alerta-dourado">Destaque: <strong>${esc(lista[0].nome)}</strong> com ${M(lista[0].total)} (${lista[0].percentual.toFixed(1)}% do total).</p>` : '';
            html += `<div class="card"><h3>Faturamento por barbeiro</h3><canvas id="g1" data-altura="260"></canvas></div>`
                + tabela(['Barbeiro', 'Atendimentos', 'Faturamento', '% do total', 'Comissão'], lista.map((b) => [b.nome, b.atendimentos, M(b.total), `${b.percentual.toFixed(1)}%`, M(b.comissao)]), [1, 2, 3, 4]);
            painel.innerHTML = html;
            G.barras($('#g1'), lista.map((b) => ({ rotulo: b.nome.split(' ')[0], valor: b.total })), { descricao: 'Faturamento por barbeiro: ' + lista.map((b) => `${b.nome} ${M(b.total)}`).join(', ') });
        } else if (rel === 'pagamentos') {
            const dados = Object.entries(S.Caixa.FORMAS).map(([k, rot]) => ({ rotulo: rot, valor: r.porForma[k], qtd: movs.filter((m) => m.formaPagamento === k).length }));
            html += `<div class="card"><h3>Faturamento por forma de pagamento</h3><canvas id="g1" data-altura="240"></canvas></div>`
                + tabela(['Forma de pagamento', 'Qtd. movimentos', 'Valor', '% do total'], dados.map((d) => [d.rotulo, d.qtd, M(d.valor), `${(r.total ? d.valor / r.total * 100 : 0).toFixed(1)}%`]), [1, 2, 3]);
            painel.innerHTML = html;
            G.rosca($('#g1'), dados, { centro: M(r.total), descricao: 'Formas de pagamento: ' + dados.map((d) => `${d.rotulo} ${M(d.valor)}`).join(', ') });
        } else if (rel === 'agenda') {
            const a = S.Indicadores.agenda(periodo.inicio, periodo.fim);
            html += kpis([['Total agendamentos', a.total], ['Concluídos', a.concluidos], ['Pendentes', a.pendentes], ['Taxa de conclusão', `${a.taxa.toFixed(0)}%`]])
                + `<div class="card"><h3>Situação da agenda</h3><canvas id="g1" data-altura="240"></canvas></div>`
                + tabela(['Indicador', 'Valor'], [['Agendados pelo site (online)', `${a.online} (${a.total ? (a.online / a.total * 100).toFixed(0) : 0}%)`], ['Agendados no balcão', a.total - a.online]], [1]);
            painel.innerHTML = html;
            G.rosca($('#g1'), [{ rotulo: 'Concluídos', valor: a.concluidos }, { rotulo: 'Pendentes', valor: a.pendentes }],
                { formatar: (v) => String(v), centro: `${a.taxa.toFixed(0)}%`, descricao: `Agenda: ${a.concluidos} concluídos e ${a.pendentes} pendentes.` });
        } else if (rel === 'clientes') {
            const lista = S.Indicadores.clientes(movs);
            html += kpis([['Clientes atendidos', lista.length], ['Ticket médio', M(r.ticketMedio)], ['Maior gasto', lista[0] ? lista[0].nome : '—'], ['Visitas', r.quantidade]])
                + `<div class="card"><h3>Clientes que mais gastaram</h3><canvas id="g1" data-altura="260"></canvas></div>`
                + tabela(['Cliente', 'Visitas', 'Total gasto', 'Ticket médio', 'Pontos fidelidade'], lista.map((c) => {
                    const cli = D.buscar('clientes', (x) => x.nome === c.nome)[0];
                    return [c.nome, c.visitas, M(c.total), M(c.ticket), cli ? cli.pontos : '—'];
                }), [1, 2, 3, 4]);
            painel.innerHTML = html;
            const top = lista.slice(0, 8);
            G.barras($('#g1'), top.map((c) => ({ rotulo: c.nome.split(' ')[0], valor: c.total })),
                { descricao: 'Clientes que mais gastaram: ' + top.map((c) => `${c.nome} ${M(c.total)}`).join(', ') });
        } else if (rel === 'servicos') {
            const lista = S.Indicadores.servicosMaisRealizados(movs);
            html += lista.length ? `<p class="alerta alerta-dourado">Mais pedido: <strong>${esc(lista[0].nome)}</strong> (${lista[0].vezes} vezes).</p>` : '';
            html += `<div class="card"><h3>Itens mais realizados</h3><canvas id="g1" data-altura="260"></canvas></div>`
                + tabela(['Serviço / produto', 'Tipo', 'Qtd. vezes', 'Valor aproximado'], lista.map((s) => [s.nome, s.tipo === 'PRODUTO' ? 'Produto' : 'Serviço', s.vezes, M(s.valor)]), [2, 3]);
            painel.innerHTML = html;
            G.barras($('#g1'), lista.slice(0, 8).map((s) => ({ rotulo: s.nome.split(/[ /]/)[0], valor: s.vezes })), { formatar: (v) => String(Math.round(v)), descricao: 'Itens mais realizados: ' + lista.slice(0, 8).map((s) => `${s.nome} ${s.vezes}`).join(', ') });
        }
    }
    function exportarCsv() {
        const csv = '\ufeff' + tabelaAtual.map((l) => l.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(';')).join('\r\n');
        const a = document.createElement('a');
        a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
        a.download = `relatorio-${rel}-${periodo.inicio}-a-${periodo.fim}.csv`;
        document.body.appendChild(a); a.click(); a.remove();
    }
    $$('[data-rel]').forEach((b) => b.addEventListener('click', () => {
        rel = b.dataset.rel;
        $$('[data-rel]').forEach((x) => x.setAttribute('aria-selected', String(x === b)));
        if (periodo) render();
    }));
    form.addEventListener('submit', (e) => { e.preventDefault(); gerar(); });
    $$('[data-periodo]').forEach((b) => b.addEventListener('click', () => definir(b.dataset.periodo)));
    $('#btn-csv').addEventListener('click', exportarCsv);
    $('#btn-imprimir').addEventListener('click', () => window.print());
    definir('mes');
})();
