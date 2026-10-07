/* Simulador de fidelidade (FidelidadePorValor + DescontoFidelidade) e consulta de pontos por telefone. */
(function () {
    'use strict';
    const { UI, Regras: R, Servicos: S } = window.Vibe;
    const { esc, $, $$ } = UI;
    const C = R.CONST;
    const form = $('#form-simulador');

    $('#sim-servicos').innerHTML = S.Catalogo.listar('SERVICO').map((s) => `
        <label class="chip"><input type="checkbox" value="${s.preco}"><span class="chip-titulo">${esc(s.nome)}</span><span class="chip-preco">${R.formatarMoeda(s.preco)}</span></label>`).join('');

    function calcular() {
        UI.limparErros(form);
        let pontos, valor;
        try {
            pontos = Math.max(0, Math.floor(Number(form.pontosAtuais.value) || 0));
            valor = R.valorMonetario(form.valorAtendimento.value, 'valorAtendimento') || 0;
            if (valor < 0) throw new R.ErroValidacao('O valor não pode ser negativo.', 'valorAtendimento');
        } catch (e) { UI.mostrarErro(form, e); return; }
        const c = R.calcularAtendimento({ itens: [{ preco: valor }], percentComissao: 0, pontosCliente: pontos, resgatar: $('#sim-resgatar').checked });
        $('#r-bruto').textContent = R.formatarMoeda(c.bruto);
        $('#r-desconto').textContent = c.desconto ? `− ${R.formatarMoeda(c.desconto)}` : R.formatarMoeda(0);
        $('#r-usados').textContent = String(c.pontosResgatados);
        $('#r-pago').textContent = R.formatarMoeda(c.pago);
        $('#r-ganhos').textContent = `+${c.pontosGanhos}`;
        $('#r-saldo').textContent = `${c.pontosFinais} pontos`;
        const resto = c.pontosFinais % C.PONTOS_POR_RESGATE;
        const falta = C.PONTOS_POR_RESGATE - resto;
        $('#r-barra span').style.width = `${resto}%`;
        $('#r-barra').setAttribute('aria-valuenow', String(resto));
        $('#r-meta-texto').textContent = `${resto}/${C.PONTOS_POR_RESGATE}`;
        // dica: quando o limite de 50% impede usar todos os blocos
        const blocos = Math.floor(pontos / C.PONTOS_POR_RESGATE);
        let dica = `Faltam ${falta} pontos (cerca de ${R.formatarMoeda(falta * C.VALOR_POR_PONTO_FIDELIDADE)} em serviços) para mais R$ ${C.VALOR_POR_RESGATE} de desconto.`;
        if ($('#sim-resgatar').checked && blocos * C.PONTOS_POR_RESGATE > c.pontosResgatados && c.bruto > 0) {
            dica = `O desconto é limitado a ${C.DESCONTO_MAXIMO_PERCENT}% do atendimento: neste valor você usa ${c.pontosResgatados} dos seus ${pontos} pontos. O restante fica guardado.`;
        } else if (blocos === 0 && $('#sim-resgatar').checked) {
            dica = `Você precisa de ${C.PONTOS_POR_RESGATE} pontos para o primeiro desconto. ` + dica;
        }
        $('#r-dica').textContent = dica;
    }

    $$('#sim-servicos input').forEach((c) => c.addEventListener('change', () => {
        const soma = $$('#sim-servicos input:checked').reduce((s, x) => s + Number(x.value), 0);
        form.valorAtendimento.value = soma.toFixed(2).replace('.', ',');
        calcular();
    }));
    form.addEventListener('input', calcular);
    form.addEventListener('submit', (e) => { e.preventDefault(); calcular(); });

    // consulta: mostra só o primeiro nome (privacidade) e o saldo
    const fc = $('#form-consulta');
    fc.addEventListener('submit', (e) => {
        e.preventDefault();
        const saida = $('#consulta-resultado');
        saida.innerHTML = '';
        const c = UI.tentar(fc, () => S.Clientes.porTelefone(R.telefone(fc.telefoneConsulta.value, 'telefoneConsulta')));
        if (c === undefined) return;
        if (!c) { saida.innerHTML = '<p class="alerta">Não encontramos este celular. Agende online e o cadastro é criado automaticamente.</p>'; return; }
        const d = R.calcularDesconto(c.pontos, Number.MAX_SAFE_INTEGER);
        saida.innerHTML = `<div class="alerta alerta-dourado"><p><strong>${esc(c.nome.split(' ')[0])}</strong>, você tem <strong>${c.pontos} pontos</strong>.</p>
            <p>${d.pontosUsados ? `Já dá para trocar ${d.pontosUsados} pontos por <strong>${R.formatarMoeda(d.valorDesconto)}</strong> de desconto (respeitando o limite de 50% do atendimento).`
            : `Faltam ${C.PONTOS_POR_RESGATE - c.pontos} pontos para o primeiro desconto.`}</p>
            <div class="progresso" aria-hidden="true"><span style="width:${c.pontos % 100}%"></span></div></div>`;
        form.pontosAtuais.value = c.pontos;
        calcular();
    });

    UI.iniciarSitePublico();
    calcular();
})();
