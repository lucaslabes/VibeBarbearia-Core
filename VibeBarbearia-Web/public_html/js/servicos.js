/* =====================================================================
   Vibe Barbearia — servicos.js
   Camada de serviços (casos de uso), como os *Service do VibeBarbearia-Core:
   valida com Vibe.Regras, confere permissão com Vibe.Auth e grava com
   Vibe.Dados. As páginas (js/paginas/*.js) só chamam estes serviços.
   ===================================================================== */
(function (global) {
    'use strict';
    const Vibe = global.Vibe = global.Vibe || {};
    const R = Vibe.Regras;
    const D = Vibe.Dados;
    const A = Vibe.Auth;
    const { ErroValidacao, ErroRegra } = R;

    // ------------------------------------------------------------------ Clientes (RF03)
    const Clientes = {
        listar(filtro = '') {
            const f = filtro.trim().toLowerCase();
            const dig = R.somenteDigitos(f);
            return D.listar('clientes')
                .filter((c) => !f || c.nome.toLowerCase().includes(f) || (dig && c.telefone.includes(dig)))
                .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
        },
        porTelefone(tel) {
            const d = R.somenteDigitos(tel);
            return D.buscar('clientes', (c) => c.telefone === d)[0] || null;
        },
        /** Valida e salva. dados.id presente = edição. */
        salvar(usuario, dados) {
            if (usuario) A.exigir(usuario, A.P.GERENCIAR_CLIENTES);
            const nome = R.obrigatorio(dados.nome, 'Nome é obrigatório.', 'nome');
            const telefone = R.telefone(dados.telefone);
            const email = R.emailOpcional(dados.email);
            const cpf = R.vazio(dados.cpf) ? null : R.cpf(dados.cpf);   // CPF opcional na web, com dígito verificador
            const outro = Clientes.porTelefone(telefone);
            if (outro && outro.id !== dados.id) throw new ErroValidacao(`Telefone já cadastrado para ${outro.nome}.`, 'telefone');
            if (cpf && D.buscar('clientes', (c) => c.cpf === cpf && c.id !== dados.id).length) {
                throw new ErroValidacao('CPF já cadastrado para outro cliente.', 'cpf');
            }
            if (dados.id) return D.atualizar('clientes', { id: dados.id, nome, telefone, email, cpf });
            return D.inserir('clientes', { nome, telefone, email, cpf, pontos: 0, ultimoCorte: null });
        },
        excluir(usuario, id) {
            A.exigir(usuario, A.P.GERENCIAR_CLIENTES);
            if (D.buscar('agendamentos', (a) => a.idCliente === id).length) {
                throw new ErroRegra('Cliente possui agendamentos no histórico e não pode ser excluído.');
            }
            D.remover('clientes', id);
        },
        historico(id) {
            return D.buscar('movimentos', (m) => m.idCliente === id).sort((a, b) => b.dataHora.localeCompare(a.dataHora));
        }
    };

    // ------------------------------------------------------------------ Funcionários (RF04–RF06)
    const Funcionarios = {
        listar(filtro = '') {
            const f = filtro.trim().toLowerCase();
            const barbeiros = D.listar('barbeiros').map((b) => ({ ...b, perfil: 'BARBEIRO' }));
            const gerentes = D.buscar('usuarios', (u) => u.perfil === 'GERENTE')
                .map((u) => ({ id: u.id, nome: u.nome, telefone: u.telefone, cpf: u.cpf, email: u.email, comissao: null, ativo: true, perfil: 'GERENTE' }));
            return [...barbeiros, ...gerentes]
                .filter((x) => !f || x.nome.toLowerCase().includes(f) || (x.cpf || '').includes(R.somenteDigitos(f) || '#'))
                .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
        },
        barbeirosAtivos() { return D.buscar('barbeiros', (b) => b.ativo).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')); },

        salvar(usuario, dados) {
            const novo = !dados.id;
            if (novo) A.exigir(usuario, A.P.CADASTRAR_FUNCIONARIO);
            else A.exigir(usuario, A.P.VISUALIZAR_FUNCIONARIOS);
            if (!novo && dados.perfil === 'GERENTE') A.exigir(usuario, A.P.GERENCIAR_GERENTES);

            const nome = R.obrigatorio(dados.nome, 'Nome é obrigatório.', 'nome');
            const telefone = R.telefone(dados.telefone);
            const email = R.emailObrigatorio(dados.email);
            const anterior = novo ? null
                : (dados.perfil === 'GERENTE' ? D.obter('usuarios', dados.id) : D.obter('barbeiros', dados.id));
            const cpfMudou = novo || R.somenteDigitos(dados.cpf) !== (anterior && anterior.cpf);
            // dígito verificador só para CPF novo/alterado (logins de teste do desktop são 111..., 222...)
            const cpf = R.cpf(dados.cpf, { exigirDigitoVerificador: cpfMudou });
            const loginEmUso = D.buscar('usuarios', (u) => u.login === cpf).filter((u) =>
                novo || (dados.perfil === 'GERENTE' ? u.id !== dados.id : u.idBarbeiro !== dados.id));
            if (loginEmUso.length) throw new ErroValidacao('Este CPF/login já está em uso. Verifique o CPF informado.', 'cpf');

            if (dados.perfil === 'GERENTE') {
                if (novo) return D.inserir('usuarios', { login: cpf, senha: R.CONST.SENHA_INICIAL_PADRAO, nome, email, perfil: 'GERENTE', idBarbeiro: null, cpf, telefone });
                return D.atualizar('usuarios', { id: dados.id, login: cpf, nome, email, cpf, telefone });
            }
            // Barbeiro: só o dono altera a comissão (gerente edita os demais dados)
            let comissao = anterior ? anterior.comissao : R.CONST.COMISSAO_PADRAO_PERCENT;
            if (A.pode(usuario, A.P.ALTERAR_COMISSAO)) {
                comissao = R.vazio(dados.comissao) ? R.CONST.COMISSAO_PADRAO_PERCENT : R.comissaoPercentual(dados.comissao);
            }
            const ativo = dados.ativo !== false;
            const especialidade = (dados.especialidade || '').trim();
            if (novo) {
                const b = D.inserir('barbeiros', { nome, telefone, cpf, email, comissao, ativo, especialidade });
                D.inserir('usuarios', { login: cpf, senha: R.CONST.SENHA_INICIAL_PADRAO, nome, email, perfil: 'BARBEIRO', idBarbeiro: b.id, cpf, telefone });
                return b;
            }
            const b = D.atualizar('barbeiros', { id: dados.id, nome, telefone, cpf, email, comissao, ativo, especialidade });
            const u = D.buscar('usuarios', (x) => x.idBarbeiro === b.id)[0];
            if (u) D.atualizar('usuarios', { id: u.id, login: cpf, nome, email, cpf, telefone });
            return b;
        },

        /** RF06: sem histórico exclui; com histórico apenas inativa. Sempre remove o login. */
        removerBarbeiro(usuario, id) {
            A.exigir(usuario, A.P.CADASTRAR_FUNCIONARIO);
            const temHistorico = D.buscar('agendamentos', (a) => a.idBarbeiro === id).length > 0
                || D.buscar('movimentos', (m) => m.idBarbeiro === id).length > 0;
            D.buscar('usuarios', (u) => u.idBarbeiro === id).forEach((u) => D.remover('usuarios', u.id));
            if (temHistorico) { D.atualizar('barbeiros', { id, ativo: false }); return 'INATIVADO'; }
            D.remover('barbeiros', id);
            return 'EXCLUIDO';
        },
        demitirGerente(usuario, idUsuario) {
            A.exigir(usuario, A.P.GERENCIAR_GERENTES);
            const alvo = D.obter('usuarios', idUsuario);
            if (!alvo || alvo.perfil !== 'GERENTE') throw new ErroRegra('Só é possível demitir usuários com perfil Gerente por esta ação.');
            if (alvo.id === usuario.id) throw new ErroRegra('Você não pode demitir o próprio usuário logado.');
            D.remover('usuarios', idUsuario);
        }
    };

    // ------------------------------------------------------------------ Catálogo (RF07)
    const Catalogo = {
        listar(tipo) {
            return D.listar('servicos').filter((s) => !tipo || s.tipo === tipo)
                .sort((a, b) => a.tipo.localeCompare(b.tipo) || a.nome.localeCompare(b.nome, 'pt-BR'));
        },
        salvar(usuario, dados) {
            A.exigir(usuario, A.P.GERENCIAR_CATALOGO);
            const nome = R.obrigatorio(dados.nome, 'Informe o nome do item.', 'nome');
            const preco = R.precoNaoNegativo(dados.preco);
            const tipo = dados.tipo === 'PRODUTO' ? 'PRODUTO' : 'SERVICO';
            const descricao = (dados.descricao || '').trim();
            if (dados.id) return D.atualizar('servicos', { id: dados.id, nome, preco, tipo, descricao });
            return D.inserir('servicos', { nome, preco, tipo, descricao });
        },
        excluir(usuario, id) { A.exigir(usuario, A.P.GERENCIAR_CATALOGO); D.remover('servicos', id); }
    };

    // ------------------------------------------------------------------ Agenda (RF08–RF10)
    const Agenda = {
        doDia(isoDia, idBarbeiro) {
            return D.buscar('agendamentos', (a) => a.dataHora.startsWith(isoDia) && (!idBarbeiro || a.idBarbeiro === idBarbeiro))
                .sort((a, b) => a.dataHora.localeCompare(b.dataHora));
        },
        pendentes(usuario) {
            return D.buscar('agendamentos', (a) => a.status === 'AGENDADO'
                && (usuario.perfil !== 'BARBEIRO' || a.idBarbeiro === usuario.idBarbeiro))
                .sort((a, b) => a.dataHora.localeCompare(b.dataHora));
        },
        conflito(idBarbeiro, dataHora, ignorarId) {
            return D.listar('agendamentos').find((a) => R.bloqueia(a, idBarbeiro, dataHora, ignorarId)) || null;
        },
        /** Valida como o AgendamentoService do Core. dados: {id?, idCliente, idBarbeiro, data (dd/MM/aaaa), hora, origem} */
        salvar(usuario, dados) {
            if (usuario) A.exigir(usuario, A.P.EDITAR_AGENDA);
            if (!dados.idCliente) throw new ErroValidacao('Selecione o cliente.', 'cliente');
            if (!dados.idBarbeiro) throw new ErroValidacao('Selecione o barbeiro.', 'barbeiro');
            const dia = R.data(dados.data);
            const hora = R.hora(dados.hora);
            const dataHora = `${dia}T${hora}`;
            const b = D.obter('barbeiros', dados.idBarbeiro);
            if (!b || !b.ativo) throw new ErroRegra('Barbeiro inativo não pode receber agendamentos.');
            if (dados.id) {
                const a = D.obter('agendamentos', dados.id);
                if (a.status === 'CONCLUIDO') throw new ErroRegra('Agendamento concluído não pode ser remarcado.');
            }
            const c = Agenda.conflito(b.id, dataHora, dados.id || null);
            if (c) {
                throw new ErroValidacao(`O barbeiro ${b.nome} já tem atendimento próximo a este horário (${R.formatarHora(c.dataHora)}). `
                    + `É preciso um intervalo mínimo de ${R.CONST.DURACAO_ATENDIMENTO_MINUTOS} minutos.`, 'hora');
            }
            if (dados.id) return D.atualizar('agendamentos', { id: dados.id, idCliente: dados.idCliente, idBarbeiro: b.id, dataHora });
            return D.inserir('agendamentos', { idCliente: dados.idCliente, idBarbeiro: b.id, dataHora, status: 'AGENDADO',
                origem: dados.origem || 'BALCAO', servicosPrevistos: dados.servicosPrevistos || [] });
        },
        cancelar(usuario, id) {
            A.exigir(usuario, A.P.EDITAR_AGENDA);
            const a = D.obter('agendamentos', id);
            if (a.status === 'CONCLUIDO') throw new ErroRegra('Agendamento concluído não pode ser cancelado.');
            D.remover('agendamentos', id);
        },
        /**
         * Agenda online: horários livres de um dia. Para cada horário informa
         * quais barbeiros estão livres pela RegraConflitoHorario. Horários que
         * já passaram (hoje) e dias fechados ficam indisponíveis.
         */
        horariosDisponiveis(isoDia, idBarbeiro, agora = new Date()) {
            const barbeiros = idBarbeiro ? [D.obter('barbeiros', idBarbeiro)].filter((b) => b && b.ativo) : Funcionarios.barbeirosAtivos();
            const aberto = R.abertoNoDia(isoDia);
            const todos = D.listar('agendamentos');
            return R.gradeHorarios().map((hora) => {
                const dataHora = `${isoDia}T${hora}`;
                const passou = R.paraDate(dataHora) <= agora;
                const livres = !aberto || passou ? [] : barbeiros.filter((b) => !todos.some((a) => R.bloqueia(a, b.id, dataHora, null)));
                return { hora, dataHora, livres, disponivel: livres.length > 0 };
            });
        },
        /** "Qualquer barbeiro": escolhe o livre com menos atendimentos no dia (equilibra a equipe). */
        escolherBarbeiroEquilibrado(livres, isoDia) {
            const carga = (b) => D.buscar('agendamentos', (a) => a.idBarbeiro === b.id && a.dataHora.startsWith(isoDia)).length;
            return livres.slice().sort((a, b) => carga(a) - carga(b) || a.nome.localeCompare(b.nome))[0];
        },
        /** Agendamento feito pelo próprio cliente no site (sem login). */
        agendarOnline({ nome, telefone, email, servicos, idBarbeiro, isoDia, hora }) {
            if (!servicos || !servicos.length) throw new ErroValidacao('Escolha pelo menos um serviço.', 'servicos');
            const nomeOk = R.obrigatorio(nome, 'Informe seu nome.', 'nome');
            const tel = R.telefone(telefone);
            const mail = R.emailOpcional(email);
            const slot = Agenda.horariosDisponiveis(isoDia, idBarbeiro || null).find((s) => s.hora === hora);
            if (!slot || !slot.disponivel) throw new ErroRegra('Este horário acabou de ficar indisponível. Escolha outro, por favor.');
            const barbeiro = idBarbeiro ? D.obter('barbeiros', idBarbeiro) : Agenda.escolherBarbeiroEquilibrado(slot.livres, isoDia);
            let cliente = Clientes.porTelefone(tel);
            if (!cliente) cliente = D.inserir('clientes', { nome: nomeOk, telefone: tel, email: mail, cpf: null, pontos: 0, ultimoCorte: null });
            const [a, m, d] = isoDia.split('-');
            const ag = Agenda.salvar(null, { idCliente: cliente.id, idBarbeiro: barbeiro.id, data: `${d}/${m}/${a}`, hora, origem: 'ONLINE', servicosPrevistos: servicos });
            return { agendamento: ag, cliente, barbeiro };
        }
    };

    // ------------------------------------------------------------------ Atendimento (RF11)
    const Atendimento = {
        simular(idAgendamento, idsItens, resgatar) {
            const ag = D.obter('agendamentos', idAgendamento);
            if (!ag) return null;
            const cliente = D.obter('clientes', ag.idCliente);
            const barbeiro = D.obter('barbeiros', ag.idBarbeiro);
            const itens = idsItens.map((id) => D.obter('servicos', id)).filter(Boolean);
            const calc = R.calcularAtendimento({ itens, percentComissao: barbeiro.comissao, pontosCliente: cliente.pontos, resgatar });
            const resgatePossivel = R.calcularDesconto(cliente.pontos, calc.bruto);
            return { ag, cliente, barbeiro, itens, calc, resgatePossivel };
        },
        finalizar(usuario, { idAgendamento, idsItens, formaPagamento, resgatar }) {
            A.exigir(usuario, A.P.REGISTRAR_ATENDIMENTO);
            if (!idAgendamento) throw new ErroValidacao('Selecione um agendamento pendente.', 'agendamento');
            const ag = D.obter('agendamentos', idAgendamento);
            if (!ag) throw new ErroValidacao('Agendamento não encontrado.', 'agendamento');
            if (ag.status === 'CONCLUIDO') throw new ErroRegra('Este agendamento já está concluído.');
            if (usuario.perfil === 'BARBEIRO' && ag.idBarbeiro !== usuario.idBarbeiro) {
                throw new ErroRegra('Acesso negado: o barbeiro só pode finalizar os próprios atendimentos.');
            }
            if (!idsItens || !idsItens.length) throw new ErroValidacao('Selecione pelo menos um serviço ou produto.', 'itens');
            if (!['DINHEIRO', 'CARTAO', 'PIX'].includes(formaPagamento)) throw new ErroValidacao('Selecione a forma de pagamento.', 'pagamento');
            const s = Atendimento.simular(idAgendamento, idsItens, !!resgatar);
            const agora = R.isoDataHora(new Date());
            // ordem do Core: resgata os pontos usados e depois soma os ganhos
            D.atualizar('clientes', { id: s.cliente.id, pontos: s.calc.pontosFinais, ultimoCorte: agora.slice(0, 10) });
            D.atualizar('agendamentos', { id: ag.id, status: 'CONCLUIDO' });
            const mov = D.inserir('movimentos', {
                dataHora: agora, idCliente: s.cliente.id, idBarbeiro: s.barbeiro.id, idAgendamento: ag.id,
                itens: s.itens.map((i) => ({ id: i.id, nome: i.nome, preco: i.preco, tipo: i.tipo })),
                descricao: s.itens.map((i) => (i.tipo === 'PRODUTO' ? 'Produto: ' : '') + i.nome).join(', '),
                valorBruto: s.calc.bruto, desconto: s.calc.desconto, valor: s.calc.pago, comissao: s.calc.comissao,
                pontosGanhos: s.calc.pontosGanhos, pontosResgatados: s.calc.pontosResgatados, formaPagamento
            });
            return { movimento: mov, ...s };
        }
    };

    // ------------------------------------------------------------------ Caixa (RF12)
    const FORMAS = { DINHEIRO: 'Dinheiro', CARTAO: 'Cartão', PIX: 'PIX' };
    const Caixa = {
        FORMAS,
        filtrar(usuario, { inicio, fim, idCliente, idBarbeiro, forma, texto } = {}) {
            if (inicio && fim) R.periodo(inicio, fim);
            const t = (texto || '').trim().toLowerCase();
            const proprio = !A.pode(usuario, A.P.VER_CAIXA_COMPLETO);
            return D.listar('movimentos').filter((m) => {
                const dia = m.dataHora.slice(0, 10);
                if (proprio && m.idBarbeiro !== usuario.idBarbeiro) return false;
                if (inicio && dia < inicio) return false;
                if (fim && dia > fim) return false;
                if (idCliente && m.idCliente !== idCliente) return false;
                if (idBarbeiro && m.idBarbeiro !== idBarbeiro) return false;
                if (forma && m.formaPagamento !== forma) return false;
                if (t) {
                    const alvo = [D.nomeCliente(m.idCliente), D.nomeBarbeiro(m.idBarbeiro), m.descricao, FORMAS[m.formaPagamento],
                        m.valor.toFixed(2), m.valor.toFixed(2).replace('.', ',')].join(' ').toLowerCase();
                    if (!alvo.includes(t)) return false;
                }
                return true;
            }).sort((a, b) => b.dataHora.localeCompare(a.dataHora));
        },
        /** ResumoCaixa do Core: total, por forma, comissão, saldo da barbearia e ticket médio. */
        resumo(movs) {
            const total = movs.reduce((s, m) => s + m.valor, 0);
            const totalComissao = movs.reduce((s, m) => s + m.comissao, 0);
            const porForma = { DINHEIRO: 0, CARTAO: 0, PIX: 0 };
            movs.forEach((m) => { porForma[m.formaPagamento] += m.valor; });
            return {
                quantidade: movs.length, total, totalComissao, porForma,
                descontos: movs.reduce((s, m) => s + (m.desconto || 0), 0),
                saldoBarbearia: total - totalComissao,
                ticketMedio: movs.length ? total / movs.length : 0
            };
        }
    };

    // ------------------------------------------------------------------ Dashboard (RF02) e Relatórios (RF13)
    const Indicadores = {
        dashboard(usuario, hoje = new Date()) {
            const isoHoje = R.isoData(hoje);
            const mes = isoHoje.slice(0, 7);
            const ano = isoHoje.slice(0, 4);
            const movs = Caixa.filtrar(usuario, {});
            const soma = (lista, campo) => lista.reduce((s, m) => s + m[campo], 0);
            const doDia = movs.filter((m) => m.dataHora.startsWith(isoHoje));
            const doMes = movs.filter((m) => m.dataHora.startsWith(mes));
            const doAno = movs.filter((m) => m.dataHora.startsWith(ano));
            const ags = D.buscar('agendamentos', (a) => a.dataHora.startsWith(isoHoje)
                && (usuario.perfil !== 'BARBEIRO' || a.idBarbeiro === usuario.idBarbeiro));
            return {
                faturamentoDia: soma(doDia, 'valor'), faturamentoMes: soma(doMes, 'valor'), faturamentoAno: soma(doAno, 'valor'),
                comissaoDia: soma(doDia, 'comissao'), comissaoMes: soma(doMes, 'comissao'),
                agendamentosDia: ags.length, pendentesDia: ags.filter((a) => a.status === 'AGENDADO').length,
                proximos: ags.filter((a) => a.status === 'AGENDADO').sort((a, b) => a.dataHora.localeCompare(b.dataHora)),
                serie: Indicadores.seriePorDia(movs, 14, hoje)
            };
        },
        seriePorDia(movs, dias, hoje = new Date()) {
            const lista = [];
            for (let i = dias - 1; i >= 0; i--) {
                const d = new Date(hoje); d.setDate(d.getDate() - i);
                const iso = R.isoData(d);
                lista.push({ rotulo: `${R.pad(d.getDate())}/${R.pad(d.getMonth() + 1)}`, valor: movs.filter((m) => m.dataHora.startsWith(iso)).reduce((s, m) => s + m.valor, 0) });
            }
            return lista;
        },
        porBarbeiro(movs) {
            const mapa = {};
            movs.forEach((m) => {
                const x = mapa[m.idBarbeiro] || (mapa[m.idBarbeiro] = { nome: D.nomeBarbeiro(m.idBarbeiro), atendimentos: 0, total: 0, comissao: 0 });
                x.atendimentos++; x.total += m.valor; x.comissao += m.comissao;
            });
            const total = movs.reduce((s, m) => s + m.valor, 0) || 1;
            return Object.values(mapa).map((x) => ({ ...x, percentual: x.total / total * 100 })).sort((a, b) => b.total - a.total);
        },
        servicosMaisRealizados(movs) {
            const mapa = {};
            movs.forEach((m) => (m.itens || []).forEach((i) => {
                const x = mapa[i.nome] || (mapa[i.nome] = { nome: i.nome, tipo: i.tipo, vezes: 0, valor: 0 });
                x.vezes++; x.valor += i.preco;
            }));
            return Object.values(mapa).sort((a, b) => b.vezes - a.vezes || b.valor - a.valor);
        },
        clientes(movs) {
            const mapa = {};
            movs.forEach((m) => {
                const x = mapa[m.idCliente] || (mapa[m.idCliente] = { nome: D.nomeCliente(m.idCliente), visitas: 0, total: 0 });
                x.visitas++; x.total += m.valor;
            });
            return Object.values(mapa).map((x) => ({ ...x, ticket: x.total / x.visitas })).sort((a, b) => b.total - a.total);
        },
        agenda(inicio, fim) {
            const ags = D.buscar('agendamentos', (a) => a.dataHora.slice(0, 10) >= inicio && a.dataHora.slice(0, 10) <= fim);
            const concluidos = ags.filter((a) => a.status === 'CONCLUIDO').length;
            const online = ags.filter((a) => a.origem === 'ONLINE').length;
            return { total: ags.length, concluidos, pendentes: ags.length - concluidos, online, taxa: ags.length ? concluidos / ags.length * 100 : 0 };
        }
    };

    Vibe.Servicos = { Clientes, Funcionarios, Catalogo, Agenda, Atendimento, Caixa, Indicadores };
})(window);
