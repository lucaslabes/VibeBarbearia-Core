/* Dashboard (TelaDashboard): indicadores por perfil, gráfico e próximos atendimentos. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Graficos: G, Dados: D } = window.Vibe;
    const { $, esc } = UI;
    const u = UI.iniciarAreaInterna(null);
    if (!u) return;
    const barbeiro = u.perfil === 'BARBEIRO';
    const k = S.Indicadores.dashboard(u);
    const agora = new Date();
    const periodo = agora.getHours() < 12 ? 'Bom dia' : agora.getHours() < 18 ? 'Boa tarde' : 'Boa noite';
    $('#saudacao').textContent = `${periodo}, ${u.nome.split(' ')[0]}!`;
    $('#data-hoje').textContent = UI.primeiraMaiuscula(agora.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }));
    if (barbeiro) document.querySelector('.topbar h1').textContent = 'Meu Dashboard';

    // mesmos títulos de card do desktop
    const cards = barbeiro ? [
        ['Meu faturamento (dia)', R.formatarMoeda(k.faturamentoDia), 'dinheiro'],
        ['Meu faturamento (mês)', R.formatarMoeda(k.faturamentoMes), 'relatorios'],
        ['Meus agendamentos (dia)', k.agendamentosDia, 'agenda'],
        ['Pendentes (dia)', k.pendentesDia, 'relogio'],
        ['Minhas comissões (dia)', R.formatarMoeda(k.comissaoDia), 'caixa'],
        ['Minhas comissões (mês)', R.formatarMoeda(k.comissaoMes), 'estrela']
    ] : [
        ['Faturamento do Dia', R.formatarMoeda(k.faturamentoDia), 'dinheiro'],
        ['Faturamento do Mês', R.formatarMoeda(k.faturamentoMes), 'relatorios'],
        ['Faturamento do Ano', R.formatarMoeda(k.faturamentoAno), 'estrela'],
        ['Agendamentos do Dia', `${k.agendamentosDia}`, 'agenda'],
        ['Comissões do Dia', R.formatarMoeda(k.comissaoDia), 'caixa'],
        ['Comissões do Mês', R.formatarMoeda(k.comissaoMes), 'clientes']
    ];
    $('#kpis').innerHTML = cards.map(([t, v, i]) => `<article class="kpi"><div class="kpi-topo"><span>${esc(t)}</span><span class="kpi-icone">${UI.icone(i)}</span></div>
        <p class="kpi-valor">${esc(v)}</p>${t === 'Agendamentos do Dia' ? `<p class="kpi-rodape">${k.pendentesDia} pendentes</p>` : ''}</article>`).join('');

    const atalhos = [];
    if (Auth.pode(u, Auth.P.EDITAR_AGENDA)) atalhos.push('<a class="btn btn-secundario btn-peq" href="agenda.html?novo=1">+ Agendamento</a>');
    if (Auth.pode(u, Auth.P.GERENCIAR_CLIENTES)) atalhos.push('<a class="btn btn-secundario btn-peq" href="clientes.html?novo=1">+ Cliente</a>');
    if (Auth.pode(u, Auth.P.REGISTRAR_ATENDIMENTO)) atalhos.push('<a class="btn btn-primario btn-peq" href="atendimento.html">Finalizar atendimento</a>');
    $('#atalhos').innerHTML = atalhos.join('');

    G.linha($('#graf-faturamento'), k.serie, { descricao: `Faturamento dos últimos 14 dias. Total: ${R.formatarMoeda(k.serie.reduce((s, x) => s + x.valor, 0))}.` });

    const podeFinalizar = Auth.pode(u, Auth.P.REGISTRAR_ATENDIMENTO);
    $('#lista-proximos').innerHTML = k.proximos.length ? k.proximos.map((a) => `
        <li class="proximo">
            <span class="proximo-hora">${R.formatarHora(a.dataHora)}</span>
            <span class="proximo-info"><strong>${esc(D.nomeCliente(a.idCliente))}</strong><small>${esc(D.nomeBarbeiro(a.idBarbeiro))}${a.origem === 'ONLINE' ? ' · <span class="badge badge-online">Online</span>' : ''}</small></span>
            ${podeFinalizar ? `<a class="btn btn-secundario btn-peq" href="atendimento.html?agendamento=${a.id}">Finalizar</a>` : '<span class="badge badge-agendado">Agendado</span>'}
        </li>`).join('') : '<li class="texto-secundario">Nenhum atendimento pendente hoje.</li>';
})();
