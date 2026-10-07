/* =====================================================================
   Vibe Barbearia — regras.js
   Regras de negócio PURAS (sem tela e sem armazenamento), espelhando o
   projeto Java VibeBarbearia-Core:
     RegrasNegocio, Validador, PoliticaSenha, ConversorEntrada (datas STRICT),
     ComissaoPercentualBarbeiro, FidelidadePorValor, DescontoFidelidade e
     RegraConflitoHorario.
   As mensagens de erro são as mesmas do desktop/Core, para manter coerência.
   ===================================================================== */
(function (global) {
    'use strict';
    const Vibe = global.Vibe = global.Vibe || {};

    /** Constantes — mesmas de vibebarbearia.core.validation.RegrasNegocio */
    const CONST = Object.freeze({
        TELEFONE_MIN_DIGITOS: 10,
        TELEFONE_MAX_DIGITOS: 11,
        CPF_DIGITOS: 11,
        EMAIL_MIN_CARACTERES: 5,
        SENHA_MIN_CARACTERES: 3,
        SENHA_INICIAL_PADRAO: '123',
        SENHA_PROVISORIA_TAMANHO: 8,
        SENHA_PROVISORIA_ALFABETO: 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789',
        COMISSAO_PADRAO_PERCENT: 40,
        COMISSAO_MIN_PERCENT: 0,
        COMISSAO_MAX_PERCENT: 100,
        VALOR_POR_PONTO_FIDELIDADE: 10,
        PONTOS_POR_RESGATE: 100,
        VALOR_POR_RESGATE: 10,
        DESCONTO_MAXIMO_PERCENT: 50,
        DURACAO_ATENDIMENTO_MINUTOS: 30,
        /* Regras novas da agenda online (só na web) */
        HORA_ABERTURA: '09:00',
        HORA_ULTIMO_HORARIO: '18:30',
        DIAS_FUNCIONAMENTO: [1, 2, 3, 4, 5, 6],   // segunda a sábado (0 = domingo)
        DIAS_ANTECEDENCIA_ONLINE: 14
    });

    /** Equivalente à ValidacaoException do Core: sabe qual campo está errado. */
    class ErroValidacao extends Error {
        constructor(mensagem, campo) {
            super(mensagem);
            this.name = 'ErroValidacao';
            this.campo = campo || null;
        }
    }
    /** Equivalente à RegraNegocioException do Core. */
    class ErroRegra extends Error {
        constructor(mensagem) { super(mensagem); this.name = 'ErroRegra'; }
    }

    // ------------------------------------------------------------------
    // Utilitários de texto e números (TextoUtil / Formatador)
    // ------------------------------------------------------------------
    const somenteDigitos = (s) => String(s == null ? '' : s).replace(/\D/g, '');
    const vazio = (s) => s == null || String(s).trim() === '';

    const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
    const formatarMoeda = (v) => moeda.format(Number(v) || 0);
    const formatarTelefone = (t) => {
        const d = somenteDigitos(t);
        if (d.length === 11) return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
        if (d.length === 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
        return d;
    };
    const formatarCpf = (c) => {
        const d = somenteDigitos(c);
        return d.length === 11 ? `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}` : d;
    };

    // ------------------------------------------------------------------
    // Validador (vibebarbearia.core.validation.Validador)
    // ------------------------------------------------------------------
    function obrigatorio(valor, mensagem, campo) {
        if (vazio(valor)) throw new ErroValidacao(mensagem, campo);
        return String(valor).trim();
    }

    /** Telefone com 10 ou 11 dígitos; devolve só os dígitos. */
    function telefone(bruto, campo = 'telefone') {
        const d = somenteDigitos(bruto);
        if (!d) throw new ErroValidacao('Telefone é obrigatório.', campo);
        if (d.length < CONST.TELEFONE_MIN_DIGITOS || d.length > CONST.TELEFONE_MAX_DIGITOS) {
            throw new ErroValidacao(`Telefone deve ter ${CONST.TELEFONE_MIN_DIGITOS} ou ${CONST.TELEFONE_MAX_DIGITOS} dígitos.`, campo);
        }
        return d;
    }

    /** Dígitos verificadores do CPF (módulo 11). Regra NOVA da web: o Core só confere os 11 dígitos. */
    function cpfDigitosConferem(d) {
        if (!/^\d{11}$/.test(d) || /^(\d)\1{10}$/.test(d)) return false;
        const dv = (base) => {
            let soma = 0;
            for (let i = 0; i < base.length; i++) soma += Number(base[i]) * (base.length + 1 - i);
            const resto = (soma * 10) % 11;
            return resto === 10 ? 0 : resto;
        };
        return dv(d.slice(0, 9)) === Number(d[9]) && dv(d.slice(0, 10)) === Number(d[10]);
    }

    /**
     * CPF: 11 dígitos (regra do Core) e, quando exigido, dígitos verificadores
     * válidos. Os logins de demonstração herdados do desktop (ex.: 11111111111)
     * não passam no dígito verificador, por isso ele só é exigido em CPF novo
     * ou alterado (ver servicos.js).
     */
    function cpf(bruto, { exigirDigitoVerificador = true, campo = 'cpf' } = {}) {
        const d = somenteDigitos(bruto);
        if (d.length !== CONST.CPF_DIGITOS) throw new ErroValidacao(`CPF deve ter ${CONST.CPF_DIGITOS} dígitos.`, campo);
        if (exigirDigitoVerificador && !cpfDigitosConferem(d)) {
            throw new ErroValidacao('CPF inválido: os dígitos verificadores não conferem.', campo);
        }
        return d;
    }

    /** Mesmo critério do Core: mínimo 5 caracteres e um @ que não está nas pontas. */
    function isEmailValido(email) {
        if (email == null) return false;
        const e = String(email).trim();
        const arroba = e.indexOf('@');
        return e.length >= CONST.EMAIL_MIN_CARACTERES && arroba > 0 && arroba < e.length - 1;
    }
    function emailObrigatorio(email, campo = 'email') {
        if (!isEmailValido(email)) throw new ErroValidacao('Informe um e-mail válido.', campo);
        return String(email).trim();
    }
    function emailOpcional(email, campo = 'email') {
        return vazio(email) ? null : emailObrigatorio(email, campo);
    }

    /** ConversorEntrada.percentual + Validador.comissao */
    function comissaoPercentual(bruto, campo = 'comissao') {
        if (vazio(bruto)) throw new ErroValidacao('Comissão é obrigatória.', campo);
        const n = Number(String(bruto).trim().replace('%', '').replace(',', '.'));
        if (Number.isNaN(n) || n < CONST.COMISSAO_MIN_PERCENT || n > CONST.COMISSAO_MAX_PERCENT) {
            throw new ErroValidacao('Comissão deve ser um número entre 0 e 100.', campo);
        }
        return n;
    }

    /** ConversorEntrada.valorMonetario: aceita "35", "35,90" e "R$ 35,90". */
    function valorMonetario(bruto, campo = 'valor') {
        if (vazio(bruto)) return null;
        const n = String(bruto).trim().toLowerCase().replace('r$', '').replace(/\s/g, '').replace(',', '.');
        if (!/^-?\d+(\.\d+)?$/.test(n)) throw new ErroValidacao('Valor inválido. Use números (ex: 35 ou 35,90).', campo);
        return Number(n);
    }
    function precoNaoNegativo(bruto, campo = 'preco') {
        const v = valorMonetario(bruto, campo);
        if (v == null || v < 0) throw new ErroValidacao('Preço inválido. Use números (ex: 35 ou 35,90).', campo);
        return v;
    }

    // ------------------------------------------------------------------
    // Datas e horas — modo STRICT (correção DEF-01 da Etapa 7):
    // "31/02/2026" é rejeitada em vez de virar 28/02/2026.
    // ------------------------------------------------------------------
    const pad = (n) => String(n).padStart(2, '0');
    const isoData = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
    const isoDataHora = (d) => `${isoData(d)}T${pad(d.getHours())}:${pad(d.getMinutes())}`;

    /** "dd/MM/aaaa" -> "aaaa-MM-dd" (lança erro se a data não existir). */
    function data(bruto, campo = 'data') {
        const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(String(bruto || '').trim());
        if (!m) throw new ErroValidacao('Data inválida. Use o formato dd/MM/aaaa.', campo);
        const [dia, mes, ano] = [Number(m[1]), Number(m[2]), Number(m[3])];
        const d = new Date(ano, mes - 1, dia);
        if (d.getFullYear() !== ano || d.getMonth() !== mes - 1 || d.getDate() !== dia) {
            throw new ErroValidacao('Data inválida: este dia não existe no calendário.', campo);
        }
        return isoData(d);
    }
    /** "HH:mm" (00:00 a 23:59). */
    function hora(bruto, campo = 'hora') {
        const m = /^(\d{2}):(\d{2})$/.exec(String(bruto || '').trim());
        if (!m || Number(m[1]) > 23 || Number(m[2]) > 59) {
            throw new ErroValidacao('Horário inválido. Use o formato HH:mm (ex: 10:00, 14:30).', campo);
        }
        return `${m[1]}:${m[2]}`;
    }
    const paraDate = (iso) => {           // "aaaa-MM-ddTHH:mm" ou "aaaa-MM-dd" -> Date local
        const [d, h = '00:00'] = String(iso).split('T');
        const [a, me, di] = d.split('-').map(Number);
        const [hh, mm] = h.split(':').map(Number);
        return new Date(a, me - 1, di, hh, mm);
    };
    const formatarData = (iso) => { const d = paraDate(iso); return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`; };
    const formatarHora = (iso) => String(iso).split('T')[1] || '';
    const formatarDataHora = (iso) => `${formatarData(iso)} ${formatarHora(iso)}`;

    /** Período: data final não pode ser anterior à inicial (Periodo do Core). */
    function periodo(inicioIso, fimIso) {
        if (fimIso < inicioIso) throw new ErroValidacao('A data final não pode ser anterior à data inicial.', 'fim');
        return { inicio: inicioIso, fim: fimIso };
    }

    // ------------------------------------------------------------------
    // PoliticaSenha
    // ------------------------------------------------------------------
    function validarNovaSenha(atual, nova, confirmacao) {
        if (vazio(atual) || vazio(nova) || vazio(confirmacao)) throw new ErroValidacao('Preencha todos os campos.');
        if (String(nova).trim().length < CONST.SENHA_MIN_CARACTERES) {
            throw new ErroValidacao(`A nova senha deve ter pelo menos ${CONST.SENHA_MIN_CARACTERES} caracteres.`, 'nova');
        }
        if (nova !== confirmacao) throw new ErroValidacao('A confirmação não confere com a nova senha.', 'confirmacao');
        if (nova === atual) throw new ErroValidacao('A nova senha deve ser diferente da atual.', 'nova');
    }
    /** Força da senha (indicador visual na web; não bloqueia além da regra do Core). */
    function forcaSenha(s) {
        s = String(s || '');
        let p = 0;
        if (s.length >= CONST.SENHA_MIN_CARACTERES) p++;
        if (s.length >= 8) p++;
        if (/[A-Z]/.test(s) && /[a-z]/.test(s)) p++;
        if (/\d/.test(s) && /[^A-Za-z0-9]/.test(s)) p++;
        return ['Muito fraca', 'Fraca', 'Média', 'Boa', 'Forte'][p];
    }
    /** GeradorSenhaAleatoria: 8 caracteres sem letras/números ambíguos. */
    function gerarSenhaProvisoria() {
        const alfa = CONST.SENHA_PROVISORIA_ALFABETO;
        const bytes = new Uint32Array(CONST.SENHA_PROVISORIA_TAMANHO);
        (global.crypto || global.msCrypto).getRandomValues(bytes);
        return Array.from(bytes, (b) => alfa[b % alfa.length]).join('');
    }
    function mascararEmail(email) {
        const [u, dom] = String(email).split('@');
        if (!dom) return email;
        return `${u.slice(0, 2)}${'*'.repeat(Math.max(1, u.length - 2))}@${dom}`;
    }

    // ------------------------------------------------------------------
    // Cálculos do atendimento
    // ------------------------------------------------------------------
    /** ComissaoPercentualBarbeiro: Math.round(valor * percent) / 100 (igual ao Java). */
    const calcularComissao = (valor, percent) => Math.round(valor * percent) / 100;

    /** FidelidadePorValor: 1 ponto a cada R$ 10,00. */
    const calcularPontos = (valor) => (valor <= 0 ? 0 : Math.floor(valor / CONST.VALOR_POR_PONTO_FIDELIDADE));

    /** DescontoFidelidade (regra nova da Etapa 7): 100 pts = R$ 10, máx. 50% do atendimento. */
    function calcularDesconto(pontosDisponiveis, valorAtendimento) {
        const nenhum = { pontosUsados: 0, valorDesconto: 0 };
        if (pontosDisponiveis <= 0 || valorAtendimento <= 0) return nenhum;
        const blocosDisponiveis = Math.floor(pontosDisponiveis / CONST.PONTOS_POR_RESGATE);
        const limite = valorAtendimento * CONST.DESCONTO_MAXIMO_PERCENT / 100;
        const blocosPeloLimite = Math.floor(limite / CONST.VALOR_POR_RESGATE);
        const blocos = Math.min(blocosDisponiveis, blocosPeloLimite);
        if (blocos === 0) return nenhum;
        return { pontosUsados: blocos * CONST.PONTOS_POR_RESGATE, valorDesconto: blocos * CONST.VALOR_POR_RESGATE };
    }

    /** AtendimentoService.finalizar: desconto reduz o valor pago; comissão e pontos sobre o valor pago. */
    function calcularAtendimento({ itens, percentComissao, pontosCliente, resgatar }) {
        const bruto = Math.round(itens.reduce((s, i) => s + Number(i.preco), 0) * 100) / 100;
        const resgate = resgatar ? calcularDesconto(pontosCliente, bruto) : { pontosUsados: 0, valorDesconto: 0 };
        const pago = Math.round((bruto - resgate.valorDesconto) * 100) / 100;
        const pontosGanhos = calcularPontos(pago);
        return {
            bruto,
            desconto: resgate.valorDesconto,
            pontosResgatados: resgate.pontosUsados,
            pago,
            comissao: calcularComissao(pago, percentComissao),
            pontosGanhos,
            pontosFinais: pontosCliente - resgate.pontosUsados + pontosGanhos
        };
    }

    // ------------------------------------------------------------------
    // RegraConflitoHorario: mesmo barbeiro, agendamento pendente, < 30 min
    // ------------------------------------------------------------------
    function horariosConflitam(existenteIso, novoIso) {
        const diffMin = Math.abs(paraDate(existenteIso) - paraDate(novoIso)) / 60000;
        return diffMin < CONST.DURACAO_ATENDIMENTO_MINUTOS;
    }
    function bloqueia(existente, idBarbeiro, novoIso, ignorarId) {
        if (!existente || !existente.dataHora) return false;
        if (ignorarId != null && ignorarId === existente.id) return false;
        return existente.status === 'AGENDADO'
            && existente.idBarbeiro === idBarbeiro
            && horariosConflitam(existente.dataHora, novoIso);
    }

    /** Horários de atendimento da agenda online (de 30 em 30 minutos). */
    function gradeHorarios() {
        const [h0, m0] = CONST.HORA_ABERTURA.split(':').map(Number);
        const [h1, m1] = CONST.HORA_ULTIMO_HORARIO.split(':').map(Number);
        const lista = [];
        for (let t = h0 * 60 + m0; t <= h1 * 60 + m1; t += CONST.DURACAO_ATENDIMENTO_MINUTOS) {
            lista.push(`${pad(Math.floor(t / 60))}:${pad(t % 60)}`);
        }
        return lista;
    }
    const abertoNoDia = (isoDia) => CONST.DIAS_FUNCIONAMENTO.includes(paraDate(isoDia).getDay());

    Vibe.Regras = Object.freeze({
        CONST, ErroValidacao, ErroRegra,
        somenteDigitos, vazio, pad, formatarMoeda, formatarTelefone, formatarCpf,
        obrigatorio, telefone, cpf, cpfDigitosConferem, isEmailValido, emailObrigatorio, emailOpcional,
        comissaoPercentual, valorMonetario, precoNaoNegativo,
        data, hora, periodo, paraDate, isoData, isoDataHora, formatarData, formatarHora, formatarDataHora,
        validarNovaSenha, forcaSenha, gerarSenhaProvisoria, mascararEmail,
        calcularComissao, calcularPontos, calcularDesconto, calcularAtendimento,
        horariosConflitam, bloqueia, gradeHorarios, abertoNoDia
    });
})(window);
