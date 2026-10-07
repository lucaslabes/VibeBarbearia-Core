/* Página inicial pública: catálogo, equipe e "próximo horário livre" calculado na hora. */
(function () {
    'use strict';
    const { UI, Regras: R, Servicos: S } = window.Vibe;
    const { esc } = UI;

    const DESCRICOES = {
        'Corte de Cabelo': 'Tesoura e máquina, com lavagem e finalização.',
        'Barba': 'Toalha quente, navalha e hidratação.',
        'Corte + Barba': 'O combo completo da casa.',
        'Sobrancelha': 'Alinhamento discreto na navalha.',
        'Hidratação Capilar': 'Tratamento para fios ressecados.',
        'Pigmentação de Barba': 'Preenche falhas e realça o desenho.',
        'Pezinho / Acabamento': 'Retoque rápido entre um corte e outro.',
        'Relaxamento': 'Redução de volume com acabamento natural.',
        'Platinado / Descoloração': 'Coloração com cuidado e matização.',
        'Design de Barba': 'Desenho personalizado para o seu rosto.'
    };

    function renderServicos() {
        const servicos = S.Catalogo.listar('SERVICO');
        UI.$('#lista-servicos').innerHTML = servicos.map((s) => `
            <article class="card servico-card">
                <h3>${esc(s.nome)}</h3>
                <p class="texto-secundario">${esc(s.descricao || DESCRICOES[s.nome] || 'Serviço da casa.')}</p>
                <div class="linha-entre">
                    <span class="preco">${R.formatarMoeda(s.preco)}</span>
                    <a class="btn btn-texto" href="agendar.html?servico=${s.id}" aria-label="Agendar ${esc(s.nome)}">Agendar →</a>
                </div>
            </article>`).join('');
        UI.$('#lista-produtos').innerHTML = S.Catalogo.listar('PRODUTO')
            .map((p) => `<li><span>${esc(p.nome)}</span><strong>${R.formatarMoeda(p.preco)}</strong></li>`).join('');
    }

    function renderEquipe() {
        UI.$('#lista-equipe').innerHTML = S.Funcionarios.barbeirosAtivos().map((b) => `
            <article class="card barbeiro-card">
                <span class="avatar avatar-grande" aria-hidden="true">${esc(UI.iniciais(b.nome))}</span>
                <h3>${esc(b.nome)}</h3>
                <p class="texto-secundario">${esc(b.especialidade || 'Barbeiro')}</p>
                <a class="btn btn-secundario btn-peq" href="agendar.html?barbeiro=${b.id}">Agendar com ${esc(b.nome.split(' ')[0])}</a>
            </article>`).join('');
    }

    /** Procura o primeiro horário livre de qualquer barbeiro nos próximos 7 dias. */
    function proximoHorarioLivre() {
        const hoje = new Date();
        for (let d = 0; d < 7; d++) {
            const dia = new Date(hoje); dia.setDate(dia.getDate() + d);
            const iso = R.isoData(dia);
            const slot = S.Agenda.horariosDisponiveis(iso, null).find((s) => s.disponivel);
            if (slot) {
                const barbeiro = S.Agenda.escolherBarbeiroEquilibrado(slot.livres, iso);
                return { iso, hora: slot.hora, barbeiro, dias: d };
            }
        }
        return null;
    }

    function renderAgora() {
        const agora = new Date();
        const hhmm = `${R.pad(agora.getHours())}:${R.pad(agora.getMinutes())}`;
        const aberto = R.abertoNoDia(R.isoData(agora)) && hhmm >= R.CONST.HORA_ABERTURA && hhmm < '19:00';
        const status = UI.$('#status-loja');
        status.textContent = aberto ? 'Aberto agora' : 'Fechado agora';
        status.classList.add(aberto ? 'aberto' : 'fechado');
        UI.$('#qtd-barbeiros').textContent = String(S.Funcionarios.barbeirosAtivos().length);
        const p = proximoHorarioLivre();
        if (!p) { UI.$('#proximo-livre').textContent = 'Agenda cheia nesta semana'; return; }
        const quando = p.dias === 0 ? 'Hoje' : p.dias === 1 ? 'Amanhã' : R.formatarData(p.iso);
        UI.$('#proximo-livre').textContent = `${quando}, ${p.hora} com ${p.barbeiro.nome.split(' ')[0]}`;
        UI.$('#link-proximo').href = `agendar.html?barbeiro=${p.barbeiro.id}&data=${p.iso}&hora=${p.hora}`;
    }

    UI.iniciarSitePublico();
    renderServicos();
    renderEquipe();
    renderAgora();
})();
