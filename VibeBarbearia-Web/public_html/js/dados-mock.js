/* =====================================================================
   Vibe Barbearia — dados-mock.js
   "Banco de dados" de demonstração no localStorage do navegador.
   Substitui o MySQL nesta etapa (sem back-end). Os dados iniciais vêm do
   script sql/vibebarbearia.sql do desktop (mesmos clientes, barbeiros,
   serviços, preços e logins de teste) e um histórico de 45 dias é gerado
   de forma determinística para alimentar o dashboard e os relatórios.
   Coleções: usuarios, clientes, barbeiros, servicos, agendamentos, movimentos.
   ===================================================================== */
(function (global) {
    'use strict';
    const Vibe = global.Vibe = global.Vibe || {};
    const R = Vibe.Regras;
    const PREFIXO = 'vibe.v1.';
    const COLECOES = ['usuarios', 'clientes', 'barbeiros', 'servicos', 'agendamentos', 'movimentos'];

    // ---------- armazenamento (com plano B em memória) ----------
    let armazenamento;
    try {
        const teste = PREFIXO + 'teste';
        global.localStorage.setItem(teste, '1');
        global.localStorage.removeItem(teste);
        armazenamento = global.localStorage;
    } catch (e) {
        const mem = {};
        armazenamento = {
            getItem: (k) => (k in mem ? mem[k] : null),
            setItem: (k, v) => { mem[k] = String(v); },
            removeItem: (k) => { delete mem[k]; }
        };
        console.warn('localStorage indisponível: os dados ficarão só na memória desta página.');
    }

    const cache = {};
    const ler = (col) => {
        if (!cache[col]) {
            try { cache[col] = JSON.parse(armazenamento.getItem(PREFIXO + col)) || []; }
            catch (e) { cache[col] = []; }
        }
        return cache[col];
    };
    const gravar = (col) => armazenamento.setItem(PREFIXO + col, JSON.stringify(cache[col]));
    const copia = (o) => (o == null ? o : JSON.parse(JSON.stringify(o)));

    // ---------- gerador pseudoaleatório com semente (dados sempre iguais) ----------
    function mulberry32(a) {
        return function () {
            a |= 0; a = (a + 0x6D2B79F5) | 0;
            let t = Math.imul(a ^ (a >>> 15), 1 | a);
            t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
            return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
        };
    }

    // ---------- dados iniciais (sql/vibebarbearia.sql do desktop) ----------
    function dadosIniciais() {
        const clientes = [
            ['Lucas Labes', '51999998888', 'lucas@email.com', 48],
            ['João Silva', '21999887766', 'joao.silva@email.com', 22],
            ['Carlos Mendes', '21988776655', 'carlos.m@email.com', 35],
            ['Pedro Oliveira', '21977665544', 'pedro.o@email.com', 12],
            ['Marcos Souza', '21966554433', 'marcos.s@email.com', 28],
            ['André Costa', '21955443322', 'andre.c@email.com', 9],
            ['Felipe Rocha', '21944332211', 'felipe.r@email.com', 17],
            ['Rafael Lima', '21933221100', 'rafael.l@email.com', 41],
            ['Gustavo Nunes', '21922110099', 'gustavo.n@email.com', 6],
            ['Bruno Alves', '21911009988', 'bruno.a@email.com', 19],
            ['Henrique Dias', '51988776655', 'henrique.d@email.com', 14],
            ['Mateus Ferreira', '51977665544', 'mateus.f@email.com', 31],
            ['Ricardo Gomes', '51966554433', 'ricardo.g@email.com', 8],
            ['Eduardo Pinto', '51955443322', 'eduardo.p@email.com', 25],
            ['Vinícius Prado', '51944332211', 'vinicius.p@email.com', 11]
        ].map(([nome, telefone, email, pontos], i) => ({ id: i + 1, nome, telefone, email, cpf: null, pontos, ultimoCorte: null }));

        const barbeiros = [
            ['Thiago Corte', '21988776655', 45, true, '11111111111', 'thiago@vibebarbearia.com', 'Fade e degradê'],
            ['Rafael Navalha', '21977665544', 40, true, '22222222222', 'rafael@vibebarbearia.com', 'Barba e navalha'],
            ['Bruno Fade', '21966554433', 42, true, '33333333333', 'bruno@vibebarbearia.com', 'Cortes modernos'],
            ['Diego Style', '21955443322', 48, true, '44444444444', 'diego@vibebarbearia.com', 'Coloração e platinado'],
            ['Gustavo Classic', '21944332211', 38, false, '66666666666', 'gustavo@vibebarbearia.com', 'Corte clássico']
        ].map(([nome, telefone, comissao, ativo, cpf, email, especialidade], i) => ({ id: i + 1, nome, telefone, comissao, ativo, cpf, email, especialidade }));

        const servicos = [
            ['Corte de Cabelo', 45.00, 'SERVICO'], ['Barba', 30.00, 'SERVICO'], ['Corte + Barba', 70.00, 'SERVICO'],
            ['Sobrancelha', 20.00, 'SERVICO'], ['Hidratação Capilar', 55.00, 'SERVICO'], ['Pigmentação de Barba', 65.00, 'SERVICO'],
            ['Pezinho / Acabamento', 25.00, 'SERVICO'], ['Relaxamento', 80.00, 'SERVICO'], ['Platinado / Descoloração', 120.00, 'SERVICO'],
            ['Design de Barba', 40.00, 'SERVICO'], ['Pomada Modeladora', 49.90, 'PRODUTO'], ['Shampoo Barba', 35.00, 'PRODUTO'],
            ['Óleo para Barba', 42.00, 'PRODUTO'], ['Cera Matte', 54.90, 'PRODUTO'], ['Balm Pós-Barba', 38.00, 'PRODUTO'],
            ['Kit Presente Vibe', 99.90, 'PRODUTO']
        ].map(([nome, preco, tipo], i) => ({ id: i + 1, nome, preco, tipo }));

        /* Logins de TESTE do desktop (README do projeto). Senhas em texto puro
           apenas porque é uma demonstração sem servidor; no sistema real a
           senha fica no servidor com hash (SenhaSha256 do Core). */
        const usuarios = [
            { id: 1, login: 'admin', senha: 'admin', nome: 'Administrador', email: 'admin@vibebarbearia.com', perfil: 'DONO', idBarbeiro: null, cpf: null, telefone: null },
            { id: 2, login: '55555555555', senha: '123', nome: 'Lucas Labes', email: 'lucas@email.com', perfil: 'GERENTE', idBarbeiro: null, cpf: '55555555555', telefone: '51999887766' },
            ...barbeiros.filter((b) => b.ativo).map((b, i) => ({
                id: 3 + i, login: b.cpf, senha: '123', nome: b.nome, email: b.email, perfil: 'BARBEIRO', idBarbeiro: b.id, cpf: b.cpf, telefone: b.telefone
            }))
        ];

        const agendamentos = [];
        const movimentos = [];
        const rnd = mulberry32(20261007);
        const escolher = (lista) => lista[Math.floor(rnd() * lista.length)];
        const hoje = new Date(); hoje.setHours(0, 0, 0, 0);
        const grade = R.gradeHorarios();
        const ativos = barbeiros.filter((b) => b.ativo);
        const svc = servicos.filter((s) => s.tipo === 'SERVICO');
        const prod = servicos.filter((s) => s.tipo === 'PRODUTO');
        const formas = ['PIX', 'PIX', 'PIX', 'CARTAO', 'CARTAO', 'DINHEIRO'];

        const concluir = (cli, bar, dataHora, itens, forma) => {
            const idAg = agendamentos.length + 1;
            agendamentos.push({ id: idAg, idCliente: cli.id, idBarbeiro: bar.id, dataHora, status: 'CONCLUIDO', origem: rnd() < 0.3 ? 'ONLINE' : 'BALCAO' });
            const resgatar = cli.pontos >= R.CONST.PONTOS_POR_RESGATE && rnd() < 0.35;
            const c = R.calcularAtendimento({ itens, percentComissao: bar.comissao, pontosCliente: cli.pontos, resgatar });
            cli.pontos = c.pontosFinais;
            cli.ultimoCorte = dataHora.slice(0, 10);
            movimentos.push({
                id: movimentos.length + 1, dataHora, idCliente: cli.id, idBarbeiro: bar.id, idAgendamento: idAg,
                itens: itens.map((i) => ({ id: i.id, nome: i.nome, preco: i.preco, tipo: i.tipo })),
                descricao: itens.map((i) => (i.tipo === 'PRODUTO' ? 'Produto: ' : '') + i.nome).join(', '),
                valorBruto: c.bruto, desconto: c.desconto, valor: c.pago, comissao: c.comissao,
                pontosGanhos: c.pontosGanhos, pontosResgatados: c.pontosResgatados, formaPagamento: forma
            });
        };
        const itensAleatorios = () => {
            const itens = [escolher(svc)];
            if (rnd() < 0.25) itens.push(escolher(svc.filter((s) => s.id !== itens[0].id)));
            if (rnd() < 0.2) itens.push(escolher(prod));
            return itens;
        };

        // Histórico dos últimos 45 dias (domingo fechado)
        for (let d = 45; d >= 1; d--) {
            const dia = new Date(hoje); dia.setDate(dia.getDate() - d);
            if (!R.abertoNoDia(R.isoData(dia))) continue;
            const qtd = 3 + Math.floor(rnd() * 6);
            const horarios = grade.slice().sort(() => rnd() - 0.5).slice(0, qtd).sort();
            horarios.forEach((h) => concluir(escolher(clientes), escolher(ativos), `${R.isoData(dia)}T${h}`, itensAleatorios(), escolher(formas)));
        }
        // Hoje: 3 concluídos e 5 agendados (igual ao script do desktop)
        const iso = R.isoData(hoje);
        [[1, 1, '09:00', [1]], [3, 2, '10:00', [3]], [8, 4, '11:00', [9]]].forEach(([c, b, h, it]) =>
            concluir(clientes[c - 1], barbeiros[b - 1], `${iso}T${h}`, it.map((i) => servicos[i - 1]), 'PIX'));
        [[2, 1, '14:00'], [5, 2, '14:30'], [4, 3, '15:30'], [6, 1, '16:00'], [7, 4, '17:00']].forEach(([c, b, h], i) =>
            agendamentos.push({ id: agendamentos.length + 1, idCliente: c, idBarbeiro: b, dataHora: `${iso}T${h}`, status: 'AGENDADO', origem: i === 1 ? 'ONLINE' : 'BALCAO' }));
        // Próximos 6 dias: agendamentos futuros
        for (let d = 1; d <= 6; d++) {
            const dia = new Date(hoje); dia.setDate(dia.getDate() + d);
            if (!R.abertoNoDia(R.isoData(dia))) continue;
            const qtd = 2 + Math.floor(rnd() * 4);
            grade.slice().sort(() => rnd() - 0.5).slice(0, qtd).sort().forEach((h) =>
                agendamentos.push({ id: agendamentos.length + 1, idCliente: escolher(clientes).id, idBarbeiro: escolher(ativos).id,
                    dataHora: `${R.isoData(dia)}T${h}`, status: 'AGENDADO', origem: rnd() < 0.4 ? 'ONLINE' : 'BALCAO' }));
        }
        return { usuarios, clientes, barbeiros, servicos, agendamentos, movimentos };
    }

    // ---------- API do repositório (mesma ideia das interfaces *Repository do Core) ----------
    const Dados = {
        listar(col) { return copia(ler(col)); },
        obter(col, id) { return copia(ler(col).find((o) => o.id === Number(id)) || null); },
        buscar(col, predicado) { return copia(ler(col).filter(predicado)); },
        inserir(col, obj) {
            const lista = ler(col);
            const novo = Object.assign({}, obj, { id: lista.reduce((m, o) => Math.max(m, o.id), 0) + 1 });
            lista.push(novo); gravar(col);
            return copia(novo);
        },
        atualizar(col, obj) {
            const lista = ler(col);
            const i = lista.findIndex((o) => o.id === obj.id);
            if (i < 0) throw new Error(`Registro ${obj.id} não encontrado em ${col}.`);
            lista[i] = Object.assign({}, lista[i], obj); gravar(col);
            return copia(lista[i]);
        },
        remover(col, id) {
            cache[col] = ler(col).filter((o) => o.id !== Number(id)); gravar(col);
        },
        /** Apaga tudo e recria os dados de demonstração. */
        resetar() {
            const d = dadosIniciais();
            COLECOES.forEach((c) => { cache[c] = d[c]; gravar(c); });
            armazenamento.setItem(PREFIXO + 'semeadoEm', new Date().toISOString());
        },
        garantirDados() {
            if (!armazenamento.getItem(PREFIXO + 'semeadoEm')) Dados.resetar();
        },
        nomeCliente(id) { const c = Dados.obter('clientes', id); return c ? c.nome : '—'; },
        nomeBarbeiro(id) { const b = Dados.obter('barbeiros', id); return b ? b.nome : '—'; }
    };

    Dados.garantirDados();
    Vibe.Dados = Dados;
})(window);
