/* Agendamento online em 4 passos. Os horários são calculados pela RegraConflitoHorario (30 min). */
(function () {
    'use strict';
    const { UI, Regras: R, Servicos: S, Dados: D } = window.Vibe;
    const { esc, $, $$ } = UI;
    const form = $('#form-agendar');
    const params = new URLSearchParams(location.search);
    const estado = {
        etapa: 1,
        servicos: new Set(params.get('servico') ? [Number(params.get('servico'))] : []),
        barbeiro: params.get('barbeiro') ? Number(params.get('barbeiro')) : null,   // 0 = sem preferência
        dia: params.get('data') || null,
        hora: params.get('hora') || null
    };
    const DIAS_SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

    // ---------- passo 1: serviços ----------
    function renderServicos() {
        $('#op-servicos').innerHTML = S.Catalogo.listar('SERVICO').map((s) => `
            <label class="chip">
                <input type="checkbox" name="servicos" value="${s.id}"${estado.servicos.has(s.id) ? ' checked' : ''}>
                <span class="chip-titulo">${esc(s.nome)}</span>
                <span class="chip-preco">${R.formatarMoeda(s.preco)}</span>
            </label>`).join('');
        $$('#op-servicos input').forEach((c) => c.addEventListener('change', () => {
            c.checked ? estado.servicos.add(Number(c.value)) : estado.servicos.delete(Number(c.value));
            $('#erro-servicos').textContent = '';
            atualizarResumo();
        }));
    }

    // ---------- passo 2: barbeiro ----------
    function renderBarbeiros() {
        const opcoes = [{ id: 0, nome: 'Sem preferência', especialidade: 'O sistema escolhe quem estiver livre e com a agenda mais leve' },
            ...S.Funcionarios.barbeirosAtivos()];
        $('#op-barbeiros').innerHTML = opcoes.map((b) => `
            <label class="chip chip-barbeiro">
                <input type="radio" name="barbeiro" value="${b.id}"${estado.barbeiro === b.id ? ' checked' : ''}>
                <span class="linha"><span class="avatar" aria-hidden="true">${b.id ? esc(UI.iniciais(b.nome)) : '★'}</span>
                <span><span class="chip-titulo">${esc(b.nome)}</span><br><span class="chip-detalhe">${esc(b.especialidade || 'Barbeiro')}</span></span></span>
            </label>`).join('');
        $$('#op-barbeiros input').forEach((r) => r.addEventListener('change', () => {
            estado.barbeiro = Number(r.value);
            $('#erro-barbeiro').textContent = '';
            if (estado.dia) renderHoras();
            atualizarResumo();
        }));
    }

    // ---------- passo 3: dia e horário ----------
    function renderDias() {
        const hoje = new Date();
        const dias = [];
        for (let i = 0; i < R.CONST.DIAS_ANTECEDENCIA_ONLINE; i++) {
            const d = new Date(hoje); d.setDate(d.getDate() + i);
            const iso = R.isoData(d);
            const aberto = R.abertoNoDia(iso);
            const livres = aberto ? S.Agenda.horariosDisponiveis(iso, estado.barbeiro || null).filter((s) => s.disponivel).length : 0;
            dias.push({ iso, d, aberto, livres });
        }
        if (!estado.dia || !dias.some((x) => x.iso === estado.dia && x.livres)) {
            const primeiro = dias.find((x) => x.livres);
            estado.dia = primeiro ? primeiro.iso : null;
        }
        $('#op-dias').innerHTML = dias.map((x) => {
            const rotulo = !x.aberto ? 'Fechado' : x.livres ? `${x.livres} livres` : 'Lotado';
            return `<button type="button" class="dia" data-dia="${x.iso}" aria-pressed="${x.iso === estado.dia}" ${x.livres ? '' : 'disabled'}
                aria-label="${DIAS_SEMANA[x.d.getDay()]} ${R.formatarData(x.iso)}: ${rotulo}">
                <span class="dia-semana">${DIAS_SEMANA[x.d.getDay()]}</span><strong>${R.pad(x.d.getDate())}/${R.pad(x.d.getMonth() + 1)}</strong><span class="dia-info">${rotulo}</span></button>`;
        }).join('');
        $$('#op-dias .dia').forEach((b) => b.addEventListener('click', () => {
            estado.dia = b.dataset.dia; estado.hora = null;
            $$('#op-dias .dia').forEach((x) => x.setAttribute('aria-pressed', String(x === b)));
            renderHoras(); atualizarResumo();
        }));
        renderHoras();
    }
    function renderHoras() {
        const alvo = $('#op-horas');
        if (!estado.dia) { alvo.innerHTML = '<p class="texto-secundario">Nenhum dia com horário livre nas próximas duas semanas.</p>'; return; }
        const slots = S.Agenda.horariosDisponiveis(estado.dia, estado.barbeiro || null);
        if (estado.hora && !slots.some((s) => s.hora === estado.hora && s.disponivel)) estado.hora = null;
        alvo.innerHTML = slots.map((s) => `<button type="button" class="horario" data-hora="${s.hora}" aria-pressed="${s.hora === estado.hora}"
            ${s.disponivel ? '' : 'disabled'} aria-label="${s.hora}${s.disponivel ? '' : ' (ocupado)'}">${s.hora}</button>`).join('');
        $$('#op-horas .horario').forEach((b) => b.addEventListener('click', () => {
            estado.hora = b.dataset.hora;
            $$('#op-horas .horario').forEach((x) => x.setAttribute('aria-pressed', String(x === b)));
            $('#erro-hora').textContent = '';
            atualizarResumo();
        }));
    }

    // ---------- resumo lateral ----------
    function itensEscolhidos() { return [...estado.servicos].map((id) => D.obter('servicos', id)).filter(Boolean); }
    function atualizarResumo() {
        const itens = itensEscolhidos();
        const total = itens.reduce((s, i) => s + i.preco, 0);
        $('#res-servicos').textContent = itens.length ? itens.map((i) => i.nome).join(', ') : '—';
        $('#res-barbeiro').textContent = estado.barbeiro === null ? '—' : estado.barbeiro === 0 ? 'Sem preferência' : D.nomeBarbeiro(estado.barbeiro);
        $('#res-quando').textContent = estado.dia && estado.hora ? `${R.formatarData(estado.dia)} às ${estado.hora}` : '—';
        $('#res-total').textContent = R.formatarMoeda(total);
        const pts = R.calcularPontos(total);
        $('#res-pontos').textContent = `${pts} ${pts === 1 ? 'ponto' : 'pontos'}`;
    }

    // ---------- navegação entre passos ----------
    function validarEtapa(n) {
        if (n === 1 && !estado.servicos.size) { $('#erro-servicos').textContent = 'Escolha pelo menos um serviço.'; return false; }
        if (n === 2 && estado.barbeiro === null) { $('#erro-barbeiro').textContent = 'Escolha um barbeiro ou “Sem preferência”.'; return false; }
        if (n === 3 && (!estado.dia || !estado.hora)) { $('#erro-hora').textContent = 'Escolha um dia e um horário livre.'; return false; }
        return true;
    }
    function irPara(n) {
        estado.etapa = n;
        $$('.etapa', form).forEach((f) => { f.hidden = Number(f.dataset.etapa) !== n; });
        $$('#passos li').forEach((li, i) => {
            li.classList.toggle('atual', i + 1 === n); li.classList.toggle('feito', i + 1 < n);
            if (i + 1 === n) li.setAttribute('aria-current', 'step'); else li.removeAttribute('aria-current');
        });
        $('#btn-voltar').hidden = n === 1;
        $('#btn-avancar').hidden = n === 4;
        $('#btn-confirmar').hidden = n !== 4;
        if (n === 3) renderDias();
        const legenda = $(`.etapa[data-etapa="${n}"] legend`, form);
        if (legenda) { legenda.setAttribute('tabindex', '-1'); legenda.focus(); }
    }
    $('#btn-avancar').addEventListener('click', () => { if (validarEtapa(estado.etapa)) irPara(estado.etapa + 1); });
    $('#btn-voltar').addEventListener('click', () => irPara(estado.etapa - 1));

    UI.validarAoSair(form, 'nome', (v) => R.obrigatorio(v, 'Informe seu nome.', 'nome'));
    UI.validarAoSair(form, 'telefone', (v) => R.telefone(v));
    UI.validarAoSair(form, 'email', (v) => R.emailOpcional(v));

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const r = UI.tentar(form, () => S.Agenda.agendarOnline({
            nome: form.nome.value, telefone: form.telefone.value, email: form.email.value,
            servicos: [...estado.servicos], idBarbeiro: estado.barbeiro || null, isoDia: estado.dia, hora: estado.hora
        }));
        if (r) mostrarConfirmacao(r);
        else if (form.querySelector('.erro-formulario').textContent) { renderDias(); }
    });

    // ---------- confirmação: arquivo .ics e WhatsApp ----------
    function mostrarConfirmacao({ agendamento, cliente, barbeiro }) {
        const itens = itensEscolhidos();
        const quando = `${R.formatarData(estado.dia)} às ${estado.hora}`;
        form.closest('.agendar-grade').hidden = true;
        $('#passos').hidden = true;
        const sec = $('#confirmacao');
        sec.hidden = false;
        $('#conf-texto').innerHTML = `${esc(cliente.nome.split(' ')[0])}, seu horário está reservado para <strong>${esc(quando)}</strong>
            com <strong>${esc(barbeiro.nome)}</strong>.<br>Serviços: ${esc(itens.map((i) => i.nome).join(', '))} · Total estimado ${R.formatarMoeda(itens.reduce((s, i) => s + i.preco, 0))}.
            <br><span class="texto-secundario">Código do agendamento: #${agendamento.id}</span>`;
        const ini = R.paraDate(agendamento.dataHora);
        const fim = new Date(ini.getTime() + R.CONST.DURACAO_ATENDIMENTO_MINUTOS * 60000);
        const f = (d) => `${d.getFullYear()}${R.pad(d.getMonth() + 1)}${R.pad(d.getDate())}T${R.pad(d.getHours())}${R.pad(d.getMinutes())}00`;
        const ics = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//Vibe Barbearia//Agenda online//PT-BR', 'BEGIN:VEVENT',
            `UID:vibe-${agendamento.id}@vibebarbearia`, `DTSTAMP:${f(new Date())}`, `DTSTART:${f(ini)}`, `DTEND:${f(fim)}`,
            `SUMMARY:Vibe Barbearia — ${itens.map((i) => i.nome).join(', ')}`, `DESCRIPTION:Atendimento com ${barbeiro.nome}.`,
            'BEGIN:VALARM', 'TRIGGER:-PT1H', 'ACTION:DISPLAY', 'DESCRIPTION:Lembrete Vibe Barbearia', 'END:VALARM',
            'END:VEVENT', 'END:VCALENDAR'].join('\r\n');
        $('#btn-ics').href = URL.createObjectURL(new Blob([ics], { type: 'text/calendar;charset=utf-8' }));
        const msg = `Agendei na Vibe Barbearia: ${itens.map((i) => i.nome).join(', ')} em ${quando} com ${barbeiro.nome}.`;
        $('#btn-whats').href = `https://wa.me/?text=${encodeURIComponent(msg)}`;
        sec.focus();
        UI.toast('Agendamento confirmado!');
    }

    UI.iniciarSitePublico();
    renderServicos();
    renderBarbeiros();
    atualizarResumo();
    irPara(1);
    window.scrollTo(0, 0);
})();
