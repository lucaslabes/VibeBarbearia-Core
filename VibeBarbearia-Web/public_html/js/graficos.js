/* =====================================================================
   Vibe Barbearia — graficos.js
   Gráficos em <canvas> sem bibliotecas externas: barras, linha e rosca.
   Usam as cores do tema (variáveis CSS), ficam nítidos em telas de alta
   densidade, redesenham ao redimensionar/trocar o tema e recebem um
   texto alternativo (aria-label) para leitores de tela.
   ===================================================================== */
(function (global) {
    'use strict';
    const Vibe = global.Vibe = global.Vibe || {};
    const registrados = new Set();

    const cor = (nome) => getComputedStyle(document.documentElement).getPropertyValue(nome).trim();
    const PALETA = ['--dourado', '--azul', '--verde', '--laranja', '--dourado-claro', '--texto-2'];
    const moedaCurta = (v) => (v >= 1000 ? `R$ ${(v / 1000).toFixed(1).replace('.', ',')} mil` : `R$ ${Math.round(v)}`);

    function preparar(canvas, alturaPadrao) {
        const dpr = global.devicePixelRatio || 1;
        const largura = canvas.parentElement.clientWidth || 300;
        const altura = Number(canvas.dataset.altura) || alturaPadrao;
        canvas.style.width = largura + 'px';
        canvas.style.height = altura + 'px';
        canvas.width = Math.round(largura * dpr);
        canvas.height = Math.round(altura * dpr);
        const ctx = canvas.getContext('2d');
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        ctx.clearRect(0, 0, largura, altura);
        ctx.font = `12px ${cor('--fonte') || 'sans-serif'}`;
        return { ctx, largura, altura };
    }

    /** Arredonda o topo do eixo para um valor "redondo" divisível em 4 faixas (ex.: 800, 1.000, 20). */
    function topoRedondo(valor) {
        const bruto = Math.max(1, valor) / 4;
        const mag = Math.pow(10, Math.floor(Math.log10(bruto)));
        const passo = [1, 2, 2.5, 5, 10].map((f) => f * mag).find((p) => p >= bruto);
        return passo * 4;
    }
    function eixos(ctx, x0, y0, largura, altura, max, formatar) {
        ctx.strokeStyle = cor('--borda'); ctx.fillStyle = cor('--texto-2'); ctx.lineWidth = 1;
        ctx.textAlign = 'right'; ctx.textBaseline = 'middle';
        for (let i = 0; i <= 4; i++) {
            const y = y0 - (altura * i) / 4;
            ctx.beginPath(); ctx.moveTo(x0, y); ctx.lineTo(x0 + largura, y); ctx.stroke();
            ctx.fillText(formatar((max * i) / 4), x0 - 6, y);
        }
    }

    function registrar(canvas, desenhar, descricao) {
        canvas.setAttribute('role', 'img');
        canvas.setAttribute('aria-label', descricao);
        canvas._desenhar = desenhar;
        registrados.add(canvas);
        desenhar();
    }

    /** Barras verticais. dados = [{rotulo, valor}] */
    function barras(canvas, dados, { formatar = moedaCurta, descricao = 'Gráfico de barras' } = {}) {
        registrar(canvas, () => {
            const { ctx, largura, altura } = preparar(canvas, 260);
            const m = { e: 64, d: 12, t: 16, b: 34 };
            const w = largura - m.e - m.d, h = altura - m.t - m.b;
            const max = topoRedondo(Math.max(1, ...dados.map((d) => d.valor)) * 1.05);
            eixos(ctx, m.e, m.t + h, w, h, max, formatar);
            const passo = w / Math.max(1, dados.length);
            const bw = Math.min(42, passo * 0.62);
            ctx.textAlign = 'center'; ctx.textBaseline = 'top';
            const pularRotulo = Math.ceil(dados.length / Math.max(1, Math.floor(w / 46)));
            dados.forEach((d, i) => {
                const x = m.e + passo * i + (passo - bw) / 2;
                const bh = (d.valor / max) * h;
                const grad = ctx.createLinearGradient(0, m.t + h - bh, 0, m.t + h);
                grad.addColorStop(0, cor('--dourado-claro')); grad.addColorStop(1, cor('--dourado-escuro'));
                ctx.fillStyle = grad;
                ctx.beginPath();
                if (ctx.roundRect) ctx.roundRect(x, m.t + h - bh, bw, bh, [4, 4, 0, 0]); else ctx.rect(x, m.t + h - bh, bw, bh);
                ctx.fill();
                if (i % pularRotulo === 0) { ctx.fillStyle = cor('--texto-2'); ctx.fillText(d.rotulo, x + bw / 2, m.t + h + 8); }
            });
        }, descricao);
    }

    /** Linha com área preenchida. dados = [{rotulo, valor}] */
    function linha(canvas, dados, { formatar = moedaCurta, descricao = 'Gráfico de linha' } = {}) {
        registrar(canvas, () => {
            const { ctx, largura, altura } = preparar(canvas, 240);
            const m = { e: 64, d: 14, t: 16, b: 34 };
            const w = largura - m.e - m.d, h = altura - m.t - m.b;
            const max = topoRedondo(Math.max(1, ...dados.map((d) => d.valor)) * 1.05);
            eixos(ctx, m.e, m.t + h, w, h, max, formatar);
            const px = (i) => m.e + (dados.length < 2 ? w / 2 : (w * i) / (dados.length - 1));
            const py = (v) => m.t + h - (v / max) * h;
            const grad = ctx.createLinearGradient(0, m.t, 0, m.t + h);
            grad.addColorStop(0, 'rgba(212,175,55,.35)'); grad.addColorStop(1, 'rgba(212,175,55,0)');
            ctx.beginPath(); ctx.moveTo(px(0), m.t + h);
            dados.forEach((d, i) => ctx.lineTo(px(i), py(d.valor)));
            ctx.lineTo(px(dados.length - 1), m.t + h); ctx.closePath(); ctx.fillStyle = grad; ctx.fill();
            ctx.beginPath(); dados.forEach((d, i) => (i ? ctx.lineTo(px(i), py(d.valor)) : ctx.moveTo(px(i), py(d.valor))));
            ctx.strokeStyle = cor('--dourado'); ctx.lineWidth = 2.5; ctx.stroke();
            ctx.textAlign = 'center'; ctx.textBaseline = 'top';
            const pular = Math.ceil(dados.length / Math.max(1, Math.floor(w / 46)));
            dados.forEach((d, i) => {
                ctx.beginPath(); ctx.arc(px(i), py(d.valor), 3.5, 0, Math.PI * 2); ctx.fillStyle = cor('--dourado'); ctx.fill();
                if (i % pular === 0) { ctx.fillStyle = cor('--texto-2'); ctx.fillText(d.rotulo, px(i), m.t + h + 8); }
            });
        }, descricao);
    }

    /** Rosca com legenda. dados = [{rotulo, valor}] */
    function rosca(canvas, dados, { formatar = (v) => Vibe.Regras.formatarMoeda(v), descricao = 'Gráfico de rosca', centro = '' } = {}) {
        registrar(canvas, () => {
            const { ctx, largura, altura } = preparar(canvas, 240);
            const total = dados.reduce((s, d) => s + d.valor, 0) || 1;
            const legendaLado = largura >= 380;
            const r = Math.min(legendaLado ? largura * 0.42 : largura, altura - (legendaLado ? 10 : 70)) / 2 - 4;
            const cx = legendaLado ? r + 10 : largura / 2, cy = legendaLado ? altura / 2 : r + 6;
            let ang = -Math.PI / 2;
            dados.forEach((d, i) => {
                const fatia = (d.valor / total) * Math.PI * 2;
                ctx.beginPath(); ctx.moveTo(cx, cy); ctx.arc(cx, cy, r, ang, ang + fatia); ctx.closePath();
                ctx.fillStyle = cor(PALETA[i % PALETA.length]); ctx.fill();
                ang += fatia;
            });
            ctx.beginPath(); ctx.arc(cx, cy, r * 0.62, 0, Math.PI * 2); ctx.fillStyle = cor('--cor-card'); ctx.fill();
            ctx.fillStyle = cor('--texto'); ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
            ctx.font = `700 14px ${cor('--fonte')}`; ctx.fillText(centro, cx, cy);
            ctx.font = `12px ${cor('--fonte')}`; ctx.textAlign = 'left';
            dados.forEach((d, i) => {
                const lx = legendaLado ? cx + r + 24 : 12 + (i % 2) * (largura / 2);
                const ly = legendaLado ? cy - (dados.length * 22) / 2 + i * 22 + 8 : cy + r + 18 + Math.floor(i / 2) * 20;
                ctx.fillStyle = cor(PALETA[i % PALETA.length]); ctx.fillRect(lx, ly - 6, 12, 12);
                ctx.fillStyle = cor('--texto');
                ctx.fillText(`${d.rotulo}: ${formatar(d.valor)} (${((d.valor / total) * 100).toFixed(0)}%)`, lx + 18, ly);
            });
        }, descricao);
    }

    let espera;
    const redesenharTodos = () => registrados.forEach((c) => (document.body.contains(c) ? c._desenhar() : registrados.delete(c)));
    global.addEventListener('resize', () => { clearTimeout(espera); espera = setTimeout(redesenharTodos, 120); });
    global.addEventListener('vibe:tema', redesenharTodos);

    Vibe.Graficos = { barras, linha, rosca, moedaCurta };
})(window);
