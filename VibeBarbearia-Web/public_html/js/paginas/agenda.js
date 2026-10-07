/* Agenda (TelaAgenda): navegação por dia, lista e linha do tempo, cadastro/remarcação com regra de conflito. */
(function () {
    'use strict';
    const { UI, Auth, Regras: R, Servicos: S, Dados: D } = window.Vibe;
    const { $, $$, esc } = UI;
    const u = UI.iniciarAreaInterna(null);
    if (!u) return;
    const podeEditar = Auth.pode(u, Auth.P.EDITAR_AGENDA);
    const podeFinalizar = Auth.pode(u, Auth.P.REGISTRAR_ATENDIMENTO);
    let dia = UI.hojeIso();
    let visao = 'lista';
    const modal = $('#modal-agendamento');
    const form = $('#form-agendamento');

    $('#aviso-consulta').hidden = podeEditar;
    $('#btn-novo').hidden = !podeEditar;
    UI.preencherSelect($('#filtro-barbeiro'), S.Funcionarios.barbeirosAtivos(), { vazio: 'Todos os barbeiros' });
    if (u.perfil === 'BARBEIRO') $('#filtro-barbeiro').value = String(u.idBarbeiro);

    function mudarDia(iso) { dia = iso; $('#filtro-data').value = R.formatarData(dia); UI.limparErros($('#form-dia')); render(); }
    const somarDias = (n) => { const d = R.paraDate(dia); d.setDate(d.getDate() + n); mudarDia(R.isoData(d)); };
    $('#dia-anterior').addEventListener('click', () => somarDias(-1));
    $('#dia-seguinte').addEventListener('click', () => somarDias(1));
    $('#dia-hoje').addEventListener('click', () => mudarDia(UI.hojeIso()));
    $('#form-dia').addEventListener('submit', (e) => {
        e.preventDefault();
        const f = e.currentTarget;
        const iso = UI.tentar(f, () => R.data(f.dataFiltro.value, 'dataFiltro'));
        if (iso) mudarDia(iso);
    });
    $('#filtro-barbeiro').addEventListener('change', render);
    $$('[data-visao]').forEach((b) => b.addEventListener('click', () => {
        visao = b.dataset.visao;
        $$('[data-visao]').forEach((x) => x.setAttribute('aria-pressed', String(x === b)));
        render();
    }));

    const badgeStatus = (a) => (a.status === 'CONCLUIDO' ? '<span class="badge badge-concluido">Concluído</span>' : '<span class="badge badge-agendado">Agendado</span>');
    const badgeOrigem = (a) => (a.origem === 'ONLINE' ? '<span class="badge badge-online">Online</span>' : '<span class="texto-secundario">Balcão</span>');

    function render() {
        const d = R.paraDate(dia);
        $('#titulo-dia').textContent = UI.primeiraMaiuscula(d.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' }));
            + (dia === UI.hojeIso() ? ' · Hoje' : '');
        const idB = Number($('#filtro-barbeiro').value) || null;
        const lista = S.Agenda.doDia(dia, idB);
        $('#visao-lista').hidden = visao !== 'lista';
        $('#visao-linha').hidden = visao !== 'linha';
        $('#tabela-agenda').innerHTML = lista.length ? lista.map((a) => `
            <tr>
                <td data-label="Hora"><strong>${R.formatarHora(a.dataHora)}</strong></td>
                <td data-label="Cliente">${esc(D.nomeCliente(a.idCliente))}</td>
                <td data-label="Barbeiro">${esc(D.nomeBarbeiro(a.idBarbeiro))}</td>
                <td data-label="Status">${badgeStatus(a)}</td>
                <td data-label="Origem">${badgeOrigem(a)}</td>
                <td class="acoes">${acoes(a)}</td>
            </tr>`).join('') : '<tr><td colspan="6" class="vazio">Nenhum agendamento neste dia</td></tr>';
        renderLinhaTempo(lista, idB);
        $$('[data-editar]').forEach((b) => b.addEventListener('click', () => abrir(D.obter('agendamentos', Number(b.dataset.editar)))));
        $$('[data-cancelar]').forEach((b) => b.addEventListener('click', () => cancelar(Number(b.dataset.cancelar))));
    }
    function acoes(a) {
        if (a.status === 'CONCLUIDO') return '';
        const minhas = u.perfil !== 'BARBEIRO' || a.idBarbeiro === u.idBarbeiro;
        return [
            podeEditar ? `<button type="button" class="btn btn-secundario btn-peq" data-editar="${a.id}">Editar</button>` : '',
            podeEditar ? `<button type="button" class="btn btn-perigo btn-peq" data-cancelar="${a.id}">Cancelar</button>` : '',
            podeFinalizar && minhas ? `<a class="btn btn-primario btn-peq" href="atendimento.html?agendamento=${a.id}">Finalizar</a>` : ''
        ].join(' ');
    }
    function renderLinhaTempo(lista, idB) {
        const barbeiros = S.Funcionarios.barbeirosAtivos().filter((b) => !idB || b.id === idB);
        const grade = R.gradeHorarios();
        let html = `<div class="lt-grade" style="--colunas:${barbeiros.length}"><div class="lt-canto"></div>`
            + barbeiros.map((b) => `<div class="lt-cabecalho"><span class="avatar" aria-hidden="true">${esc(UI.iniciais(b.nome))}</span>${esc(b.nome.split(' ')[0])}</div>`).join('');
        grade.forEach((h) => {
            html += `<div class="lt-hora">${h}</div>`;
            barbeiros.forEach((b) => {
                const a = lista.find((x) => x.idBarbeiro === b.id && R.formatarHora(x.dataHora) === h);
                const ocupado = !a && lista.some((x) => R.bloqueia(x, b.id, `${dia}T${h}`, null));
                html += a ? `<div class="lt-celula lt-${a.status.toLowerCase()}" title="${esc(D.nomeCliente(a.idCliente))}"><strong>${esc(D.nomeCliente(a.idCliente).split(' ')[0])}</strong><small>${a.status === 'CONCLUIDO' ? 'Concluído' : 'Agendado'}${a.origem === 'ONLINE' ? ' · online' : ''}</small></div>`
                    : `<div class="lt-celula ${ocupado ? 'lt-bloqueado' : 'lt-livre'}" aria-label="${b.nome} ${h} ${ocupado ? 'bloqueado (intervalo)' : 'livre'}"></div>`;
            });
        });
        $('#linha-tempo').innerHTML = html + '</div>';
    }

    // ---------- modal ----------
    function opcoesModal(sel) {
        UI.preencherSelect(form.cliente, S.Clientes.listar(), { vazio: 'Selecione…', texto: (c) => `${c.nome} — ${R.formatarTelefone(c.telefone)}`, selecionado: sel && sel.idCliente });
        const barbeiros = S.Funcionarios.barbeirosAtivos();
        UI.preencherSelect(form.barbeiro, barbeiros, { vazio: 'Selecione…', selecionado: sel && sel.idBarbeiro });
    }
    function abrir(a) {
        UI.limparErros(form);
        form.reset();
        opcoesModal(a);
        $('#modal-ag-titulo').textContent = a ? 'Editar Agendamento' : 'Novo Agendamento';
        form.id.value = a ? a.id : '';
        form.data.value = R.formatarData(a ? a.dataHora : dia);
        form.hora.value = a ? R.formatarHora(a.dataHora) : '';
        sugerir();
        UI.abrirModal(modal);
    }
    /** Sugere horários livres do barbeiro escolhido (mesma regra do agendamento online, mas sem horário de funcionamento rígido). */
    function sugerir() {
        const alvo = $('#sugestoes');
        let iso = null;
        try { iso = R.data(form.data.value); } catch (e) { /* data incompleta */ }
        const idB = Number(form.barbeiro.value);
        if (!iso || !idB) { alvo.innerHTML = '<p class="ajuda">Escolha o barbeiro e uma data válida para ver os horários livres.</p>'; return; }
        const idAtual = Number(form.id.value) || null;
        const todos = D.listar('agendamentos');
        const livres = R.gradeHorarios().filter((h) => !todos.some((a) => R.bloqueia(a, idB, `${iso}T${h}`, idAtual)));
        alvo.innerHTML = livres.length ? livres.map((h) => `<button type="button" class="horario" data-h="${h}" aria-pressed="${form.hora.value === h}">${h}</button>`).join('')
            : '<p class="ajuda">Nenhum horário livre neste dia.</p>';
        $$('#sugestoes .horario').forEach((b) => b.addEventListener('click', () => {
            form.hora.value = b.dataset.h;
            $$('#sugestoes .horario').forEach((x) => x.setAttribute('aria-pressed', String(x === b)));
            UI.limparErros(form);
        }));
    }
    form.barbeiro.addEventListener('change', sugerir);
    form.data.addEventListener('input', () => { if (form.data.value.length === 10) sugerir(); });
    UI.validarAoSair(form, 'data', (v) => R.data(v));
    UI.validarAoSair(form, 'hora', (v) => R.hora(v));

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = Number(form.id.value) || null;
        const ok = UI.tentar(form, () => S.Agenda.salvar(u, {
            id, idCliente: Number(form.cliente.value) || null, idBarbeiro: Number(form.barbeiro.value) || null,
            data: form.data.value, hora: form.hora.value
        }));
        if (!ok) return;
        modal.close();
        UI.toast(id ? 'Agendamento atualizado com sucesso!' : 'Agendamento criado com sucesso!');
        mudarDia(ok.dataHora.slice(0, 10));
    });
    async function cancelar(id) {
        const a = D.obter('agendamentos', id);
        if (!await UI.confirmar('Cancelar agendamento', `<p>Cancelar o horário de <strong>${esc(D.nomeCliente(a.idCliente))}</strong> às ${R.formatarHora(a.dataHora)}?</p>`, { textoOk: 'Cancelar horário', perigo: true })) return;
        try { S.Agenda.cancelar(u, id); UI.toast('Agendamento cancelado.'); render(); } catch (err) { UI.toast(err.message, 'erro'); }
    }

    $('#btn-novo').addEventListener('click', () => abrir(null));
    mudarDia(dia);
    if (podeEditar && new URLSearchParams(location.search).get('novo')) abrir(null);
})();
