"""Wireframes de baixa fidelidade (tons de cinza) da Vibe Barbearia Web — gera SVG."""
import os, sys
OUT = sys.argv[1]; os.makedirs(OUT, exist_ok=True)
FONTE = "'Segoe UI', 'DejaVu Sans', Arial, sans-serif"
C = dict(fundo='#ffffff', moldura='#f4f4f4', traco='#8f8f8f', barra='#d4d4d4', escuro='#5f5f5f', medio='#bdbdbd', claro='#ebebeb', texto='#3a3a3a')

class S:
    def __init__(s, w, h, titulo=''):
        s.w, s.h, s.e = w, h, []
        s.rect(0, 0, w, h, fill=C['fundo'], stroke='none', r=0)
    def rect(s, x, y, w, h, fill=C['fundo'], stroke=C['traco'], r=6, dash=False, sw=1.4):
        d = ' stroke-dasharray="6 4"' if dash else ''
        s.e.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')
    def text(s, x, y, t, size=13, weight='normal', fill=C['texto'], anchor='start', italic=False):
        t = t.replace('&', '&amp;').replace('<', '&lt;')
        st = ' font-style="italic"' if italic else ''
        s.e.append(f'<text x="{x}" y="{y}" font-family="{FONTE}" font-size="{size}" font-weight="{weight}" fill="{fill}" text-anchor="{anchor}"{st}>{t}</text>')
    def line(s, x1, y1, x2, y2, stroke=C['traco'], sw=1.4, dash=False):
        d = ' stroke-dasharray="5 4"' if dash else ''
        s.e.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{stroke}" stroke-width="{sw}"{d}/>')
    def circ(s, cx, cy, r, fill=C['medio'], stroke=C['traco']):
        s.e.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{fill}" stroke="{stroke}" stroke-width="1.2"/>')
    def bars(s, x, y, w, n=3, h=8, gap=15, cor=C['barra']):
        for i in range(n):
            ww = w if i < n - 1 else w * 0.6
            s.rect(x, y + i * gap, ww, h, fill=cor, stroke='none', r=4)
    def btn(s, x, y, w, h, rot, prim=False, size=12):
        s.rect(x, y, w, h, fill=C['escuro'] if prim else C['fundo'], stroke=C['escuro'], r=6)
        s.text(x + w / 2, y + h / 2 + size * .36, rot, size, 'bold', '#ffffff' if prim else C['texto'], 'middle')
    def inp(s, x, y, w, rot=None, h=32, ph='', sel=False):
        if rot: s.text(x, y - 7, rot, 11, 'bold', C['texto'])
        s.rect(x, y, w, h, fill=C['moldura'], stroke=C['traco'], r=5)
        if ph: s.text(x + 10, y + h / 2 + 4, ph, 11, fill='#9a9a9a')
        if sel: s.text(x + w - 16, y + h / 2 + 4, '▾', 12, fill=C['texto'])
    def img(s, x, y, w, h, rot=''):
        s.rect(x, y, w, h, fill=C['claro'], stroke=C['traco'], r=4)
        s.line(x, y, x + w, y + h, C['medio']); s.line(x + w, y, x, y + h, C['medio'])
        if rot:
            s.rect(x + w / 2 - len(rot) * 3.4 - 8, y + h / 2 - 11, len(rot) * 6.8 + 16, 22, fill='#ffffff', stroke='none', r=4)
            s.text(x + w / 2, y + h / 2 + 4, rot, 11, 'bold', C['texto'], 'middle')
    def card(s, x, y, w, h, tit=None, destaque=False):
        s.rect(x, y, w, h, fill=C['fundo'], stroke=C['escuro'] if destaque else C['traco'], r=8, sw=2.2 if destaque else 1.4)
        if tit: s.text(x + 16, y + 26, tit, 14, 'bold')
    def chart_linha(s, x, y, w, h, rot='Gráfico de linha'):
        s.line(x + 30, y + 10, x + 30, y + h - 20); s.line(x + 30, y + h - 20, x + w - 10, y + h - 20)
        for i in range(4): s.line(x + 30, y + 10 + i * (h - 30) / 4, x + w - 10, y + 10 + i * (h - 30) / 4, C['claro'], 1)
        import math
        pts = ' '.join(f'{x + 30 + i * (w - 40) / 13:.0f},{y + h - 30 - (0.45 + 0.35 * math.sin(i * 1.3) + (0.1 if i % 3 else -0.2)) * (h - 50):.0f}' for i in range(14))
        s.e.append(f'<polyline points="{pts}" fill="none" stroke="{C["escuro"]}" stroke-width="2"/>')
        s.text(x + w / 2, y + h - 4, rot, 10, fill='#888', anchor='middle', italic=True)
    def chart_barras(s, x, y, w, h, n=7, rot='Gráfico de barras'):
        s.line(x + 30, y + 10, x + 30, y + h - 20); s.line(x + 30, y + h - 20, x + w - 10, y + h - 20)
        alt = [.5, .35, .8, .2, .95, .3, .45, .6, .7, .4]
        bw = (w - 60) / n * .55
        for i in range(n):
            bh = alt[i % len(alt)] * (h - 40)
            s.rect(x + 40 + i * (w - 60) / n, y + h - 20 - bh, bw, bh, fill=C['medio'], stroke=C['traco'], r=2)
        s.text(x + w / 2, y + h - 4, rot, 10, fill='#888', anchor='middle', italic=True)
    def tabela(s, x, y, w, cols, linhas=6, rh=34, acoes=0, badges=()):
        s.rect(x, y, w, rh, fill=C['medio'], stroke=C['traco'], r=6)
        cw = w / len(cols)
        for i, c in enumerate(cols): s.text(x + 12 + i * cw, y + rh / 2 + 4, c, 11, 'bold')
        for r in range(linhas):
            yy = y + rh * (r + 1)
            s.rect(x, yy, w, rh, fill=C['fundo'] if r % 2 == 0 else C['moldura'], stroke=C['claro'], r=0, sw=1)
            for i in range(len(cols) - (1 if acoes else 0)):
                if i in badges: s.rect(x + 12 + i * cw, yy + 9, 64, 16, fill=C['fundo'], stroke=C['escuro'], r=8)
                else: s.rect(x + 12 + i * cw, yy + 13, cw * (.55 + .25 * ((r + i) % 3) / 2), 8, fill=C['barra'], stroke='none', r=4)
            for a in range(acoes): s.btn(x + w - 12 - (acoes - a) * 62, yy + 6, 56, rh - 12, ['Editar', 'Excluir', 'Finalizar', 'Hist.'][a % 4], a == acoes - 1, 10)
        return y + rh * (linhas + 1)
    def check(s, x, y, rot, marcado=False):
        s.rect(x, y, 14, 14, fill=C['escuro'] if marcado else C['fundo'], stroke=C['escuro'], r=3)
        s.text(x + 22, y + 12, rot, 12)
    def chip(s, x, y, w, h, rot, ativo=False, sub=None):
        s.rect(x, y, w, h, fill=C['medio'] if ativo else C['fundo'], stroke=C['escuro'] if ativo else C['traco'], r=6, sw=2 if ativo else 1.2)
        s.text(x + w / 2, y + (h / 2 + 4 if not sub else h / 2 - 2), rot, 11, 'bold', C['texto'], 'middle')
        if sub: s.text(x + w / 2, y + h / 2 + 13, sub, 10, fill='#777', anchor='middle')
    def nota(s, x, y, n, txt=None):
        s.circ(x, y, 11, fill='#ffffff', stroke=C['escuro']); s.text(x, y + 4, str(n), 11, 'bold', C['texto'], 'middle')
    def salvar(s, nome):
        svg = f'<svg xmlns="http://www.w3.org/2000/svg" width="{s.w}" height="{s.h}" viewBox="0 0 {s.w} {s.h}">' + ''.join(s.e) + '</svg>'
        open(os.path.join(OUT, nome + '.svg'), 'w', encoding='utf-8').write(svg)

MENU = ['Dashboard', 'Agenda', 'Clientes', 'Funcionários', 'Serviços e produtos', 'Atendimento', 'Caixa', 'Relatórios', 'Alterar senha']
W, H = 1280, 800

def casca_app(s, titulo, ativo, sub='Subtítulo da tela'):
    s.rect(0, 0, 230, s.h, fill=C['claro'], stroke=C['traco'], r=0)
    s.circ(32, 32, 16); s.text(56, 30, 'VIBE', 16, 'bold'); s.text(56, 44, 'BARBEARIA', 9, fill='#777')
    for i, m in enumerate(MENU):
        y = 76 + i * 40
        if i == ativo: s.rect(0, y - 4, 230, 36, fill=C['escuro'], stroke='none', r=0)
        s.rect(20, y + 7, 16, 16, fill='#ffffff' if i == ativo else C['medio'], stroke='none', r=3)
        s.text(46, y + 20, m, 12, 'bold' if i == ativo else 'normal', '#ffffff' if i == ativo else C['texto'])
    yb = 76 + len(MENU) * 40 + 16
    s.card(14, yb, 202, 52); s.circ(38, yb + 26, 14); s.text(60, yb + 22, 'Nome do usuário', 12, 'bold'); s.text(60, yb + 38, 'Perfil', 10, fill='#777')
    s.btn(14, yb + 62, 202, 36, '⇥  Sair')
    s.rect(230, 0, s.w - 230, 62, fill=C['fundo'], stroke=C['traco'], r=0)
    s.text(256, 30, titulo, 19, 'bold'); s.text(256, 48, sub, 11, fill='#777')
    s.circ(s.w - 40, 31, 16, fill='#ffffff'); s.text(s.w - 40, 36, '☀', 13, anchor='middle')
    return 256, 86, s.w - 256 - 26

def casca_publica(s, ativo=None):
    s.rect(0, 0, s.w, 62, fill=C['claro'], stroke=C['traco'], r=0)
    s.circ(90, 31, 16); s.text(114, 30, 'VIBE', 16, 'bold'); s.text(114, 44, 'BARBEARIA', 9, fill='#777')
    x = 640
    for i, m in enumerate(['Serviços', 'Barbeiros', 'Fidelidade', 'Área da equipe']):
        s.text(x, 36, m, 12, 'bold' if ativo == i else 'normal'); x += len(m) * 7 + 34
    s.btn(x, 15, 130, 32, 'Agendar online', True); s.circ(s.w - 70, 31, 16, fill='#ffffff'); s.text(s.w - 70, 36, '☀', 13, anchor='middle')
    s.rect(0, s.h - 52, s.w, 52, fill=C['claro'], stroke=C['traco'], r=0)
    s.bars(90, s.h - 30, 260, 1); s.bars(s.w - 360, s.h - 30, 270, 1)

# ---------------- PÁGINAS PÚBLICAS ----------------
def inicio():
    s = S(W, 1180); casca_publica(s)
    s.rect(0, 62, W, 360, fill=C['moldura'], stroke=C['traco'], r=0)
    s.bars(90, 110, 140, 1, h=8); s.text(90, 175, 'Seu estilo, no seu horário.', 40, 'bold')
    s.bars(90, 205, 440, 2); s.btn(90, 260, 140, 40, 'Agendar agora', True); s.btn(242, 260, 160, 40, 'Simular meus pontos')
    s.card(800, 100, 390, 250, 'Agora na Vibe', True)
    for i, r in enumerate(['● Aberto agora', 'Próximo horário livre', 'Barbeiros atendendo', 'Horário']):
        s.text(816, 160 + i * 34, r, 12); s.bars(1060, 152 + i * 34, 110, 1)
    s.btn(816, 296, 358, 38, 'Reservar este horário', True); s.nota(780, 100, 1)
    s.text(W / 2, 470, 'Serviços e preços', 22, 'bold', anchor='middle'); s.bars(W / 2 - 200, 485, 400, 1)
    for i in range(8):
        x, y = 90 + (i % 4) * 280, 520 + (i // 4) * 150
        s.card(x, y, 260, 130); s.bars(x + 16, y + 20, 140, 1, h=10); s.bars(x + 16, y + 46, 220, 2); s.bars(x + 16, y + 100, 70, 1, h=12, cor=C['medio']); s.text(x + 200, y + 110, 'Agendar →', 11, 'bold')
    s.nota(70, 520, 2)
    s.text(W / 2, 860, 'Nossa equipe', 22, 'bold', anchor='middle')
    for i in range(4):
        x = 90 + i * 280; s.card(x, 890, 260, 140); s.circ(x + 50, 940, 30); s.bars(x + 96, 925, 130, 2); s.btn(x + 16, 984, 228, 30, 'Agendar com ele')
    s.rect(90, 1050, 1100, 70, fill=C['claro'], stroke=C['traco'], r=8); s.text(110, 1092, 'Programa de fidelidade: R$ 10 = 1 ponto · 100 pontos = R$ 10 de desconto', 14, 'bold'); s.btn(1020, 1066, 150, 36, 'Simular pontos', True)
    s.salvar('01_inicio_desktop')

def agendar():
    s = S(W, 820); casca_publica(s)
    s.text(90, 110, 'Agendar online', 26, 'bold'); s.bars(90, 124, 380, 1)
    for i, p in enumerate(['1 Serviços', '2 Barbeiro', '3 Data e horário', '4 Seus dados']):
        x = 90 + i * 280; s.text(x, 170, p, 12, 'bold' if i == 2 else 'normal'); s.rect(x, 182, 268, 4, fill=C['escuro'] if i <= 2 else C['barra'], stroke='none', r=2)
    s.nota(70, 170, 1)
    s.card(90, 210, 740, 520, '3. Quando?')
    for i in range(8): s.chip(106 + i * 88, 250, 80, 62, ['QUA', 'QUI', 'SEX', 'SÁB', 'DOM', 'SEG', 'TER', 'QUA'][i], i == 0, 'Fechado' if i == 4 else 'n livres')
    s.nota(80, 280, 2); s.text(106, 345, 'Horários disponíveis', 12, 'bold')
    for i in range(20):
        x, y = 106 + (i % 8) * 88, 360 + (i // 8) * 50
        s.chip(x, y, 80, 40, f'{9 + i // 2:02d}:{"30" if i % 2 else "00"}', i == 11)
        if i in (0, 1, 2, 5, 6, 9): s.line(x + 20, y + 20, x + 60, y + 20, C['escuro'])
    s.text(106, 530, 'riscado = ocupado (regra de 30 min)', 10, fill='#777', italic=True); s.nota(80, 380, 3)
    s.btn(620, 670, 90, 40, 'Voltar'); s.btn(720, 670, 96, 40, 'Continuar', True)
    s.card(860, 210, 330, 290, 'Resumo', True)
    for i, r in enumerate(['Serviços', 'Barbeiro', 'Data e hora']): s.text(876, 262 + i * 52, r, 11, fill='#777'); s.bars(876, 272 + i * 52, 200, 1, h=10)
    s.line(876, 420, 1174, 420, C['claro']); s.text(876, 450, 'Total estimado', 13); s.text(1174, 452, 'R$ 00,00', 20, 'bold', anchor='end'); s.bars(876, 470, 240, 1)
    s.nota(1200, 210, 4)
    s.salvar('02_agendar_desktop')

def fidelidade():
    s = S(W, 860); casca_publica(s, 2)
    s.text(90, 110, 'Programa de fidelidade', 26, 'bold'); s.bars(90, 124, 560, 1)
    s.card(90, 160, 640, 470, 'Simulador de pontos')
    s.inp(106, 220, 290, 'Meus pontos atuais'); s.inp(416, 220, 290, 'Visitas por mês'); s.text(106, 290, 'Serviços escolhidos', 11, 'bold')
    for i in range(10): s.chip(106 + (i % 4) * 152, 302 + (i // 4) * 56, 144, 48, 'Serviço', i in (2, 5), 'R$ 00,00')
    s.check(106, 480, 'Usar meus pontos para desconto', True)
    s.card(760, 160, 430, 470, 'Resultado', True)
    for i, r in enumerate(['Valor do atendimento', 'Desconto com pontos', 'Total a pagar', 'Pontos ganhos', 'Saldo após visita']):
        s.text(776, 220 + i * 40, r, 12); s.bars(1080, 212 + i * 40, 90, 1)
    s.text(776, 440, 'Progresso até o próximo resgate', 11, 'bold'); s.rect(776, 452, 398, 14, fill=C['claro'], stroke=C['traco'], r=7); s.rect(776, 452, 240, 14, fill=C['escuro'], stroke='none', r=7)
    s.bars(776, 490, 380, 3)
    s.card(90, 650, 1100, 110, 'Consultar meus pontos'); s.inp(106, 700, 860, None, ph='Celular cadastrado'); s.btn(980, 700, 190, 32, 'Consultar', True)
    s.nota(70, 160, 1); s.nota(740, 160, 2); s.nota(70, 650, 3)
    s.salvar('03_fidelidade_desktop')

def acesso(nome, form):
    s = S(W, H)
    s.rect(0, 0, 640, H, fill=C['claro'], stroke=C['traco'], r=0)
    s.circ(320, 300, 46, fill=C['medio']); s.text(320, 400, 'VIBE', 44, 'bold', anchor='middle'); s.text(320, 425, 'BARBEARIA', 12, fill='#777', anchor='middle'); s.bars(220, 460, 200, 2)
    s.text(24, 775, '← Voltar para o site', 11); s.circ(W - 36, 34, 16, fill='#ffffff')
    form(s); s.salvar(nome)

def login():
    def f(s):
        x = 760; s.text(x, 90, 'Acessar sistema', 24, 'bold'); s.bars(x, 106, 260, 1)
        s.inp(x, 160, 400, 'Usuário *'); s.text(x, 210, 'Dono: login livre. Gerente e barbeiro: CPF.', 10, fill='#777')
        s.inp(x, 250, 400, 'Senha *'); s.btn(x, 310, 400, 40, 'ENTRAR', True); s.text(x + 200, 380, 'Esqueci minha senha', 12, anchor='middle')
        s.rect(x, 410, 400, 250, fill=C['fundo'], stroke=C['traco'], r=8, dash=True); s.text(x + 16, 436, 'Acessos de demonstração', 13, 'bold'); s.bars(x + 16, 448, 300, 1)
        for i, (p, c) in enumerate([('Dono', 'admin / admin'), ('Gerente', '555.555.555-55 / 123'), ('Barbeiro', '111.111.111-11 / 123')]):
            s.rect(x + 16, 476 + i * 46, 368, 38, fill=C['moldura'], stroke=C['traco'], r=6); s.text(x + 30, 500 + i * 46, p, 12, 'bold'); s.text(x + 370, 500 + i * 46, c, 11, anchor='end')
        s.text(x + 16, 636, 'Restaurar dados de demonstração', 12, 'bold')
        s.nota(740, 160, 1); s.nota(740, 410, 2)
    acesso('04_login_desktop', f)

def recuperar():
    def f(s):
        x = 760; s.text(x, 90, 'Esqueci minha senha', 24, 'bold'); s.bars(x, 106, 330, 2)
        s.inp(x, 180, 400, 'Login (CPF ou usuário) *'); s.btn(x, 236, 400, 40, 'Gerar senha provisória', True); s.text(x + 200, 306, 'Voltar ao login', 12, anchor='middle')
        s.card(x, 340, 400, 200, 'Senha provisória gerada', True); s.bars(x + 16, 380, 360, 3); s.rect(x + 16, 440, 200, 34, fill=C['moldura'], stroke=C['escuro'], r=6); s.text(x + 30, 462, 'XXXX XXXX', 14, 'bold'); s.btn(x + 16, 490, 140, 34, 'Ir para o login', True)
        s.nota(740, 340, 1)
    acesso('05_recuperar_senha_desktop', f)

# ---------------- ÁREA INTERNA ----------------
def dashboard():
    s = S(W, 900); x, y, w = casca_app(s, 'Dashboard', 0, 'Visão geral do dia')
    s.text(x, y + 20, 'Boa tarde, <nome>!', 20, 'bold'); s.bars(x, y + 32, 220, 1)
    s.btn(x + w - 420, y, 120, 34, '+ Agendamento'); s.btn(x + w - 290, y, 100, 34, '+ Cliente'); s.btn(x + w - 180, y, 180, 34, 'Finalizar atendimento', True)
    kw = (w - 32) / 3
    for i, k in enumerate(['Faturamento do Dia', 'Faturamento do Mês', 'Faturamento do Ano', 'Agendamentos do Dia', 'Comissões do Dia', 'Comissões do Mês']):
        kx, ky = x + (i % 3) * (kw + 16), y + 66 + (i // 3) * 112
        s.card(kx, ky, kw, 96, destaque=True); s.text(kx + 16, ky + 26, k, 11, fill='#666'); s.text(kx + 16, ky + 66, 'R$ 0.000,00' if i != 3 else '8', 28, 'bold')
    s.nota(x - 10, y + 66, 1)
    cy = y + 66 + 224 + 6
    s.card(x, cy, w * .62, 380, 'Faturamento — últimos 14 dias'); s.chart_linha(x + 16, cy + 46, w * .62 - 32, 310, 'Canvas: linha com área (faturamento diário)')
    lx = x + w * .62 + 16; lw = w * .38 - 16
    s.card(lx, cy, lw, 380, 'Próximos atendimentos de hoje'); s.text(lx + lw - 16, cy + 26, 'Ver agenda', 11, 'bold', anchor='end')
    for i in range(5):
        yy = cy + 46 + i * 64; s.rect(lx + 16, yy, lw - 32, 56, fill=C['fundo'], stroke=C['traco'], r=6); s.text(lx + 28, yy + 33, '14:00', 13, 'bold'); s.bars(lx + 86, yy + 16, 120, 2); s.btn(lx + lw - 106, yy + 13, 78, 30, 'Finalizar')
    s.nota(x - 10, cy, 2); s.nota(lx - 10, cy, 3)
    s.salvar('06_dashboard_desktop')

def barra_agenda(s, x, y, w):
    s.card(x, y, w, 80)
    s.circ(x + 30, y + 48, 13, '#fff'); s.btn(x + 50, y + 34, 50, 28, 'Hoje'); s.circ(x + 120, y + 48, 13, '#fff')
    s.inp(x + 150, y + 32, 140, 'Data (dd/mm/aaaa)', ph='07/10/2026'); s.btn(x + 298, y + 32, 40, 32, 'Ir')
    s.inp(x + 356, y + 32, 230, 'Barbeiro', ph='Todos os barbeiros', sel=True)
    s.chip(x + 604, y + 34, 56, 28, 'Lista', True); s.chip(x + 664, y + 34, 110, 28, 'Linha do tempo')
    s.btn(x + w - 176, y + 30, 160, 36, 'Novo Agendamento', True)

def agenda():
    s = S(W, H); x, y, w = casca_app(s, 'Agenda de Horários', 1, 'Agendamentos por dia e por barbeiro')
    barra_agenda(s, x, y, w); s.nota(x - 10, y, 1)
    s.text(x, y + 120, 'Quarta-feira, 07/10/2026', 15, 'bold')
    s.tabela(x, y + 140, w, ['Hora', 'Cliente', 'Barbeiro', 'Status', 'Origem', 'Ações'], 8, 40, acoes=3, badges=(3, 4)); s.nota(x - 10, y + 140, 2)
    s.text(x, 770, 'Alternativa "Linha do tempo": grade horário × barbeiro com blocos livres/agendados/concluídos', 11, fill='#777', italic=True)
    s.salvar('07_agenda_desktop')

def agenda_linha():
    s = S(W, H); x, y, w = casca_app(s, 'Agenda de Horários', 1, 'Agendamentos por dia e por barbeiro')
    barra_agenda(s, x, y, w); s.text(x, y + 120, 'Quarta-feira, 07/10/2026 — visão linha do tempo', 15, 'bold')
    cw = (w - 70) / 4
    for b in range(4): s.circ(x + 84 + b * cw, y + 152, 12); s.bars(x + 104 + b * cw, y + 148, 80, 1)
    for r in range(12):
        yy = y + 172 + r * 42; s.text(x + 10, yy + 26, f'{9 + r // 2:02d}:{"30" if r % 2 else "00"}', 11)
        for b in range(4):
            ocup = (r, b) in [(0, 3), (2, 2), (4, 1), (10, 3), (11, 2)]
            s.rect(x + 70 + b * cw, yy, cw - 6, 36, fill=C['medio'] if ocup else C['moldura'], stroke=C['traco'], r=4)
            if ocup: s.rect(x + 70 + b * cw, yy, 5, 36, fill=C['escuro'], stroke='none', r=2); s.bars(x + 84 + b * cw, yy + 10, 90, 2, h=6, gap=11, cor='#ffffff')
    s.nota(x - 10, y + 172, 1)
    s.salvar('08_agenda_linha_tempo_desktop')

def agenda_modal():
    s = S(W, H); x, y, w = casca_app(s, 'Agenda de Horários', 1, 'Agendamentos por dia e por barbeiro')
    barra_agenda(s, x, y, w); s.tabela(x, y + 140, w, ['Hora', 'Cliente', 'Barbeiro', 'Status', 'Origem', 'Ações'], 6, 40, acoes=3)
    s.rect(0, 0, W, H, fill='#000000', stroke='none', r=0); s.e[-1] = s.e[-1].replace('fill="#000000"', 'fill="#000000" fill-opacity="0.35"')
    mx, my, mw = 420, 110, 480
    s.rect(mx, my, mw, 560, fill='#ffffff', stroke=C['escuro'], r=10, sw=2); s.text(mx + 20, my + 34, 'Novo Agendamento', 17, 'bold'); s.circ(mx + mw - 30, my + 28, 13, '#fff'); s.text(mx + mw - 30, my + 33, '×', 14, anchor='middle')
    s.line(mx, my + 52, mx + mw, my + 52, C['claro'])
    s.inp(mx + 20, my + 90, mw - 40, 'Cliente *', sel=True); s.inp(mx + 20, my + 155, mw - 40, 'Barbeiro *', sel=True)
    s.inp(mx + 20, my + 220, 210, 'Data (dd/mm/aaaa) *'); s.inp(mx + 250, my + 220, 210, 'Horário (HH:mm) *', ph='14:15')
    s.rect(mx + 250, my + 220, 210, 32, fill='none', stroke=C['escuro'], r=5, sw=2.4)
    s.text(mx + 250, my + 270, '⚠ O barbeiro X já tem atendimento', 11, 'bold'); s.text(mx + 250, my + 285, 'próximo a este horário (14:00).', 11, 'bold')
    s.text(mx + 20, my + 320, 'Horários livres deste barbeiro (clique para usar)', 11, 'bold')
    for i in range(18): s.chip(mx + 20 + (i % 6) * 74, my + 334 + (i // 6) * 42, 66, 34, f'{10 + i // 2:02d}:{"30" if i % 2 else "00"}')
    s.line(mx, my + 496, mx + mw, my + 496, C['claro']); s.btn(mx + mw - 220, my + 512, 96, 34, 'Cancelar'); s.btn(mx + mw - 114, my + 512, 94, 34, 'Salvar', True)
    s.nota(mx - 14, my + 236, 1); s.nota(mx - 14, my + 340, 2)
    s.salvar('09_agenda_modal_conflito_desktop')

def clientes():
    s = S(W, H); x, y, w = casca_app(s, 'Gerenciamento de Clientes', 2, 'Cadastro, histórico e pontos de fidelidade')
    s.card(x, y, w, 80); s.inp(x + 16, y + 34, w - 200, 'Buscar por nome ou telefone', ph='Ex.: Silva ou 99988'); s.btn(x + w - 170, y + 32, 154, 36, 'Novo Cliente', True)
    s.text(x, y + 112, 'N clientes', 11, fill='#777')
    s.tabela(x, y + 128, w, ['Nome', 'Telefone', 'E-mail', 'Pontos', 'Último corte', 'Ações'], 10, 38, acoes=3, badges=(3,))
    s.nota(x - 10, y, 1); s.nota(x - 10, y + 128, 2)
    s.salvar('10_clientes_desktop')

def funcionarios():
    s = S(W, 860); x, y, w = casca_app(s, 'Funcionários', 3, 'Barbeiros e gerentes')
    s.card(x, y, w, 80); s.inp(x + 16, y + 34, w - 450, 'Buscar', ph='Nome ou CPF'); s.inp(x + w - 420, y + 34, 220, 'Perfil', ph='Todos', sel=True); s.btn(x + w - 186, y + 32, 170, 36, 'Cadastrar Funcionário', True)
    cw = (w - 32) / 3
    for i in range(6):
        cx, cy = x + (i % 3) * (cw + 16), y + 100 + (i // 3) * 290
        s.card(cx, cy, cw, 270); s.circ(cx + 40, cy + 40, 22); s.bars(cx + 74, cy + 30, 140, 1, h=10); s.rect(cx + 74, cy + 46, 70, 16, fill='#fff', stroke=C['escuro'], r=8)
        for r, rot in enumerate(['CPF (login)', 'Telefone', 'E-mail', 'Comissão', 'Especialidade']): s.text(cx + 20, cy + 96 + r * 26, rot, 11, fill='#666'); s.bars(cx + 130, cy + 88 + r * 26, cw - 160, 1)
        s.btn(cx + 20, cy + 222, 80, 30, 'Editar'); s.btn(cx + 108, cy + 222, 100, 30, 'Inativar')
    s.nota(x - 10, y + 100, 1)
    s.salvar('11_funcionarios_desktop')

def servicos():
    s = S(W, 820); x, y, w = casca_app(s, 'Serviços e produtos', 4, 'Catálogo e preços (somente Dono)')
    for i, t in enumerate(['Todos', 'Serviços', 'Produtos']): s.text(x + i * 100, y + 20, t, 13, 'bold' if i == 0 else 'normal')
    s.rect(x, y + 30, 50, 3, fill=C['escuro'], stroke='none', r=1); s.line(x, y + 33, x + w, y + 33, C['claro']); s.btn(x + w - 170, y, 170, 34, 'Novo serviço / produto', True)
    cw = (w - 48) / 4
    for i in range(12):
        cx, cy = x + (i % 4) * (cw + 16), y + 56 + (i // 4) * 170
        s.card(cx, cy, cw, 150); s.rect(cx + 16, cy + 16, 64, 16, fill='#fff', stroke=C['escuro'], r=8); s.text(cx + cw - 16, cy + 30, 'R$ 00,00', 14, 'bold', anchor='end')
        s.bars(cx + 16, cy + 56, cw - 60, 2); s.btn(cx + 16, cy + 106, 70, 28, 'Editar'); s.btn(cx + 94, cy + 106, 74, 28, 'Excluir')
    s.salvar('12_servicos_desktop')

def atendimento():
    s = S(W, 900); x, y, w = casca_app(s, 'Registro de Atendimento', 5, 'Finalize o atendimento, calcule comissão e pontos')
    fw = w * .58; s.card(x, y, fw, 790)
    s.inp(x + 16, y + 40, fw - 32, 'Agendamento (pendente) *', ph='14:30 · Cliente — Barbeiro', sel=True)
    s.rect(x + 16, y + 86, fw - 32, 50, fill=C['moldura'], stroke=C['traco'], r=6); s.circ(x + 42, y + 111, 14); s.bars(x + 66, y + 100, 220, 2, gap=13); s.rect(x + fw - 130, y + 102, 98, 18, fill='#fff', stroke=C['escuro'], r=9); s.text(x + fw - 81, y + 115, '119 pontos', 10, 'bold', anchor='middle')
    s.rect(x + 16, y + 160, fw - 32, 330, fill=C['fundo'], stroke=C['traco'], r=6); s.text(x + 28, y + 165, ' Serviços e produtos * ', 12, 'bold')
    s.inp(x + 30, y + 182, fw - 60, None, ph='Filtrar itens...')
    for i in range(7):
        yy = y + 226 + i * 36; s.rect(x + 30, yy, fw - 60, 30, fill=C['medio'] if i in (1, 4) else C['fundo'], stroke=C['traco'], r=5); s.check(x + 40, yy + 8, '', i in (1, 4)); s.bars(x + 66, yy + 11, 160, 1); s.text(x + fw - 42, yy + 20, 'R$ 00,00', 11, 'bold', anchor='end')
    s.text(x + 28, y + 520, 'Forma de pagamento *', 12, 'bold')
    for i, t in enumerate(['PIX', 'Cartão', 'Dinheiro']): s.chip(x + 28 + i * ((fw - 56) / 3), y + 532, (fw - 56) / 3 - 8, 42, t, i == 0)
    s.rect(x + 16, y + 600, fw - 32, 70, fill=C['fundo'], stroke=C['traco'], r=6, dash=True); s.check(x + 30, y + 618, 'Resgatar pontos de fidelidade', True); s.bars(x + 30, y + 646, 330, 1)
    s.btn(x + 16, y + 700, fw - 32, 42, 'Finalizar Atendimento', True)
    rx, rw = x + fw + 20, w - fw - 20
    s.card(rx, y, rw, 380, 'Resumo do atendimento (recibo ao vivo)', True)
    for i, t in enumerate(['Item 1', 'Item 2', 'Subtotal', 'Desconto fidelidade']): s.text(rx + 16, y + 64 + i * 28, t, 12); s.text(rx + rw - 16, y + 64 + i * 28, 'R$ 00,00', 12, 'bold', anchor='end')
    s.line(rx + 16, y + 180, rx + rw - 16, y + 180, C['traco'], dash=True); s.text(rx + 16, y + 214, 'Total a pagar', 13); s.text(rx + rw - 16, y + 216, 'R$ 000,00', 22, 'bold', anchor='end')
    for i, t in enumerate(['Comissão do barbeiro (%)', 'Pontos ganhos', 'Saldo de pontos após']): s.text(rx + 16, y + 256 + i * 28, t, 12); s.bars(rx + rw - 90, y + 248 + i * 28, 74, 1)
    s.nota(x - 10, y + 86, 1); s.nota(x - 10, y + 160, 2); s.nota(x - 10, y + 600, 3); s.nota(rx - 10, y, 4)
    s.salvar('13_atendimento_desktop')

def caixa():
    s = S(W, 860); x, y, w = casca_app(s, 'Caixa', 6, 'Movimentos financeiros')
    s.card(x, y, w, 120)
    for i, (r, wd) in enumerate([('Período de', 150), ('até', 150), ('Barbeiro', 200), ('Pagamento', 180)]): s.inp(x + 16 + sum([150, 150, 200, 180][:i]) + i * 14, y + 34, wd, r, sel=i >= 2)
    for i, t in enumerate(['Aplicar', 'Hoje', 'Este mês', 'Exportar CSV']): s.btn(x + 16 + i * 110, y + 78, 100, 30, t, i == 0)
    kw = (w - 48) / 4
    for i, k in enumerate(['Total recebido', 'Atendimentos', 'Comissões', 'Por forma de pagamento']):
        kx = x + i * (kw + 16); s.card(kx, y + 140, kw, 90, destaque=True); s.text(kx + 16, y + 166, k, 11, fill='#666'); s.text(kx + 16, y + 204, 'R$ 0.000,00', 22, 'bold')
    s.text(x, y + 270, 'Movimentos', 15, 'bold')
    s.tabela(x, y + 286, w, ['Data/hora', 'Cliente', 'Barbeiro', 'Itens', 'Pagamento', 'Desconto', 'Valor', 'Comissão'], 10, 36, badges=(4,))
    s.nota(x - 10, y, 1); s.nota(x - 10, y + 140, 2)
    s.salvar('14_caixa_desktop')

def relatorios():
    s = S(W, 900); x, y, w = casca_app(s, 'Relatórios', 7, 'Desempenho do negócio por período')
    s.card(x, y, w, 80); s.inp(x + 16, y + 34, 140, 'Período de'); s.inp(x + 170, y + 34, 140, 'até')
    for i, t in enumerate(['Gerar', 'Hoje', 'Este mês', 'Últimos 30 dias', 'Exportar CSV', 'Imprimir']): s.btn(x + 330 + i * 112, y + 34, 104, 32, t, i == 0)
    for i, t in enumerate(['Resumo geral', 'Por barbeiro', 'Pagamentos', 'Agenda', 'Clientes', 'Serviços']): s.text(x + i * 130, y + 120, t, 13, 'bold' if i == 0 else 'normal')
    s.rect(x, y + 130, 100, 3, fill=C['escuro'], stroke='none', r=1); s.line(x, y + 133, x + w, y + 133, C['claro'])
    kw = (w - 48) / 4
    for i in range(4): kx = x + i * (kw + 16); s.card(kx, y + 156, kw, 80, destaque=True); s.bars(kx + 16, y + 176, 100, 1); s.text(kx + 16, y + 218, 'R$ 0.000,00', 20, 'bold')
    s.card(x, y + 256, w, 300, 'Gráfico do relatório selecionado'); s.chart_barras(x + 16, y + 300, w - 32, 240, 10, 'Canvas: barras / linha / rosca conforme a aba')
    s.tabela(x, y + 576, w, ['Coluna 1', 'Coluna 2', 'Coluna 3', 'Coluna 4'], 5, 36)
    s.nota(x - 10, y, 1); s.nota(x - 10, y + 110, 2); s.nota(x - 10, y + 256, 3)
    s.salvar('15_relatorios_desktop')

def alterar():
    s = S(W, H); x, y, w = casca_app(s, 'Alterar senha', 8, 'Segurança da conta')
    cx = x + (w - 520) / 2; s.card(cx, y + 20, 520, 480, 'Alterar senha'); s.bars(cx + 16, y + 56, 300, 1)
    s.inp(cx + 16, y + 110, 488, 'Senha atual *'); s.inp(cx + 16, y + 180, 488, 'Nova senha *'); s.text(cx + 16, y + 228, 'Mínimo de 3 caracteres; diferente da atual.', 10, fill='#777')
    s.rect(cx + 16, y + 240, 488, 8, fill=C['claro'], stroke='none', r=4); s.rect(cx + 16, y + 240, 300, 8, fill=C['escuro'], stroke='none', r=4); s.text(cx + 16, y + 264, 'Força da senha: média', 10, 'bold')
    s.inp(cx + 16, y + 310, 488, 'Confirmar nova senha *'); s.btn(cx + 290, y + 400, 100, 36, 'Cancelar'); s.btn(cx + 400, y + 400, 104, 36, 'Salvar', True)
    s.nota(cx - 14, y + 240, 1)
    s.salvar('16_alterar_senha_desktop')

# ---------------- MOBILE ----------------
MW, MH = 360, 740
def m_pub(s):
    s.rect(0, 0, MW, 56, fill=C['claro'], stroke=C['traco'], r=0); s.circ(30, 28, 14); s.text(52, 33, 'VIBE', 15, 'bold')
    s.circ(MW - 70, 28, 14, '#fff'); s.rect(MW - 46, 14, 30, 28, fill='#fff', stroke=C['traco'], r=6); [s.rect(MW - 39, 21 + i * 6, 16, 2, fill=C['escuro'], stroke='none', r=1) for i in range(3)]
def m_app(s, titulo):
    s.rect(0, 0, MW, 56, fill=C['fundo'], stroke=C['traco'], r=0); s.rect(12, 14, 30, 28, fill='#fff', stroke=C['traco'], r=6)
    [s.rect(19, 21 + i * 6, 16, 2, fill=C['escuro'], stroke='none', r=1) for i in range(3)]; s.text(54, 34, titulo, 16, 'bold'); s.circ(MW - 30, 28, 14, '#fff')
def m_inicio():
    s = S(MW, MH); m_pub(s); s.rect(0, 56, MW, 300, fill=C['moldura'], stroke=C['traco'], r=0)
    s.bars(16, 80, 110, 1); s.text(16, 130, 'Seu estilo, no', 28, 'bold'); s.text(16, 164, 'seu horário.', 28, 'bold'); s.bars(16, 184, 320, 3)
    s.btn(16, 240, 150, 40, 'Agendar agora', True); s.btn(16, 292, 180, 40, 'Simular meus pontos')
    s.card(16, 376, MW - 32, 230, 'Agora na Vibe', True)
    for i in range(4): s.bars(32, 420 + i * 34, 120, 1); s.bars(220, 420 + i * 34, 90, 1)
    s.btn(32, 556, MW - 64, 36, 'Reservar este horário', True); s.text(MW / 2, 650, 'Serviços e preços', 18, 'bold', anchor='middle'); s.card(16, 670, MW - 32, 70)
    s.nota(MW - 36, 66, 1); s.salvar('17_inicio_mobile')
def m_agendar():
    s = S(MW, MH); m_pub(s); s.text(16, 92, 'Agendar online', 20, 'bold')
    for i in range(4): s.rect(16 + i * 84, 108, 78, 4, fill=C['escuro'] if i <= 2 else C['barra'], stroke='none', r=2)
    s.text(16, 132, '3. Data e horário', 13, 'bold')
    for i in range(5): s.chip(16 + i * 76, 146, 70, 60, ['QUA', 'QUI', 'SEX', 'SÁB', 'DOM'][i], i == 0, 'livres')
    s.text(MW - 16, 222, 'arraste →', 10, fill='#777', anchor='end', italic=True)
    for i in range(20):
        xx, yy = 16 + (i % 4) * 84, 236 + (i // 4) * 46; s.chip(xx, yy, 76, 38, f'{9 + i // 2:02d}:{"30" if i % 2 else "00"}', i == 11)
        if i in (0, 1, 5, 6): s.line(xx + 18, yy + 19, xx + 58, yy + 19, C['escuro'])
    s.rect(0, MH - 90, MW, 90, fill=C['fundo'], stroke=C['escuro'], r=0, sw=2); s.text(16, MH - 62, 'Total estimado', 12); s.text(MW - 16, MH - 60, 'R$ 00,00', 18, 'bold', anchor='end')
    s.btn(16, MH - 46, 100, 36, 'Voltar'); s.btn(126, MH - 46, MW - 142, 36, 'Continuar', True)
    s.nota(MW - 30, MH - 100, 1); s.salvar('18_agendar_mobile')
def m_login():
    s = S(MW, MH); s.rect(0, 0, MW, 220, fill=C['claro'], stroke=C['traco'], r=0); s.circ(MW / 2, 80, 34); s.text(MW / 2, 150, 'VIBE', 30, 'bold', anchor='middle'); s.text(16, 200, '← Voltar para o site', 11)
    s.text(16, 260, 'Acessar sistema', 20, 'bold'); s.inp(16, 300, MW - 32, 'Usuário *'); s.inp(16, 370, MW - 32, 'Senha *'); s.btn(16, 420, MW - 32, 42, 'ENTRAR', True); s.text(MW / 2, 488, 'Esqueci minha senha', 12, anchor='middle')
    s.rect(16, 510, MW - 32, 210, fill='#fff', stroke=C['traco'], r=8, dash=True); s.text(30, 536, 'Acessos de demonstração', 13, 'bold')
    for i, p in enumerate(['Dono', 'Gerente', 'Barbeiro']): s.rect(30, 552 + i * 44, MW - 60, 36, fill=C['moldura'], stroke=C['traco'], r=6); s.text(42, 575 + i * 44, p, 12, 'bold'); s.bars(200, 567 + i * 44, 100, 1)
    s.salvar('19_login_mobile')
def m_dashboard():
    s = S(MW, MH); m_app(s, 'Dashboard'); s.text(16, 90, 'Boa tarde, <nome>!', 18, 'bold'); s.btn(16, 110, 120, 30, '+ Agendamento'); s.btn(144, 110, 90, 30, '+ Cliente'); s.btn(16, 148, 170, 30, 'Finalizar atendimento', True)
    for i in range(3): s.card(16, 196 + i * 96, MW - 32, 84, destaque=True); s.bars(32, 214 + i * 96, 120, 1); s.text(32, 262 + i * 96, 'R$ 0.000,00', 24, 'bold')
    s.text(MW / 2, 494, '… KPIs empilhados (1 coluna)', 10, fill='#777', anchor='middle', italic=True)
    s.card(16, 508, MW - 32, 220, 'Faturamento — 14 dias'); s.chart_linha(24, 546, MW - 48, 170, 'Canvas redimensiona')
    s.nota(28, 72, 1); s.salvar('20_dashboard_mobile')
def m_menu():
    s = S(MW, MH); m_app(s, 'Dashboard'); s.rect(0, 0, MW, MH, fill='#000', stroke='none', r=0); s.e[-1] = s.e[-1].replace('fill="#000"', 'fill="#000" fill-opacity="0.35"')
    s.rect(0, 0, 280, MH, fill=C['claro'], stroke=C['traco'], r=0); s.circ(32, 32, 16); s.text(56, 37, 'VIBE', 16, 'bold')
    for i, m in enumerate(MENU):
        yy = 74 + i * 46
        if i == 0: s.rect(0, yy - 6, 280, 42, fill=C['escuro'], stroke='none', r=0)
        s.rect(22, yy + 6, 16, 16, fill='#fff' if i == 0 else C['medio'], stroke='none', r=3); s.text(50, yy + 20, m, 13, 'bold' if i == 0 else 'normal', '#fff' if i == 0 else C['texto'])
    s.card(16, 500, 248, 56); s.circ(42, 528, 15); s.bars(66, 518, 120, 2); s.btn(16, 566, 248, 38, '⇥  Sair')
    s.nota(300, 40, 1); s.salvar('21_menu_mobile')
def m_agenda():
    s = S(MW, MH); m_app(s, 'Agenda'); s.card(16, 70, MW - 32, 210)
    s.circ(44, 102, 13, '#fff'); s.btn(64, 88, 50, 28, 'Hoje'); s.circ(134, 102, 13, '#fff'); s.inp(32, 148, 230, 'Data'); s.btn(270, 148, 58, 32, 'Ir'); s.inp(32, 206, MW - 64, 'Barbeiro', sel=True)
    s.chip(32, 246, 56, 26, 'Lista', True); s.chip(94, 246, 110, 26, 'Linha do tempo')
    s.btn(MW - 186, 292, 170, 36, 'Novo Agendamento', True); s.text(16, 356, 'Quarta-feira, 07/10/2026', 14, 'bold')
    for i in range(2):
        yy = 372 + i * 178; s.card(16, yy, MW - 32, 166)
        for r, rot in enumerate(['Hora', 'Cliente', 'Barbeiro', 'Status']): s.text(30, yy + 26 + r * 26, rot, 11, fill='#666'); s.bars(MW - 140, yy + 18 + r * 26, 110, 1)
        s.btn(30, yy + 124, 80, 30, 'Editar'); s.btn(118, yy + 124, 86, 30, 'Cancelar'); s.btn(212, yy + 124, 104, 30, 'Finalizar', True)
    s.nota(MW - 26, 372, 1); s.salvar('22_agenda_mobile')
def m_atendimento():
    s = S(MW, MH); m_app(s, 'Atendimento'); s.inp(16, 96, MW - 32, 'Agendamento (pendente) *', sel=True)
    s.rect(16, 140, MW - 32, 46, fill=C['moldura'], stroke=C['traco'], r=6); s.circ(40, 163, 13); s.bars(62, 154, 140, 2, gap=12)
    s.text(16, 212, 'Serviços e produtos *', 12, 'bold')
    for i in range(6): yy = 222 + i * 38; s.rect(16, yy, MW - 32, 32, fill=C['medio'] if i in (1, 3) else '#fff', stroke=C['traco'], r=5); s.check(26, yy + 9, '', i in (1, 3)); s.bars(52, yy + 12, 150, 1); s.text(MW - 26, yy + 21, 'R$ 00', 11, 'bold', anchor='end')
    s.text(16, 470, 'Pagamento *', 12, 'bold')
    for i, t in enumerate(['PIX', 'Cartão', 'Dinheiro']): s.chip(16 + i * 112, 480, 104, 38, t, i == 0)
    s.rect(16, 530, MW - 32, 50, fill='#fff', stroke=C['traco'], r=6, dash=True); s.check(28, 547, 'Resgatar pontos', True)
    s.rect(0, MH - 130, MW, 130, fill='#fff', stroke=C['escuro'], r=0, sw=2); s.text(16, MH - 102, 'Total a pagar', 12); s.text(MW - 16, MH - 100, 'R$ 000,00', 20, 'bold', anchor='end')
    s.bars(16, MH - 86, 300, 2, gap=13); s.btn(16, MH - 50, MW - 32, 40, 'Finalizar Atendimento', True)
    s.nota(MW - 26, MH - 140, 1); s.salvar('23_atendimento_mobile')

# ---------------- MAPA DE NAVEGAÇÃO ----------------
def mapa():
    s = S(1400, 860)
    s.text(700, 40, 'Mapa de páginas e fluxo de navegação — Vibe Barbearia Web', 22, 'bold', anchor='middle')
    s.e.append('<defs><marker id="seta" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto"><path d="M0,0 L10,5 L0,10 z" fill="#5f5f5f"/></marker></defs>')
    def no(x, y, w, h, tit, sub='', forte=False, perfis=''):
        s.rect(x, y, w, h, fill=C['medio'] if forte else '#ffffff', stroke=C['escuro'], r=8, sw=2 if forte else 1.4)
        s.text(x + w / 2, y + (h / 2 if not sub else h / 2 - 4), tit, 13, 'bold', anchor='middle')
        if sub: s.text(x + w / 2, y + h / 2 + 12, sub, 10, fill='#555', anchor='middle')
        if perfis:
            for i, p in enumerate(perfis): s.circ(x + w - 12 - (len(perfis) - 1 - i) * 20, y, 9, '#ffffff'); s.text(x + w - 12 - (len(perfis) - 1 - i) * 20, y + 4, p, 10, 'bold', anchor='middle')
        return (x, y, w, h)
    def seta(a, b, rot='', lado='h', dash=False):
        if lado == 'h': x1, y1, x2, y2 = a[0] + a[2], a[1] + a[3] / 2, b[0], b[1] + b[3] / 2
        elif lado == 'v': x1, y1, x2, y2 = a[0] + a[2] / 2, a[1] + a[3], b[0] + b[2] / 2, b[1]
        else: x1, y1, x2, y2 = lado
        d = ' stroke-dasharray="6 4"' if dash else ''
        s.e.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="#5f5f5f" stroke-width="1.6" marker-end="url(#seta)"{d}/>')
        if rot: s.text((x1 + x2) / 2, (y1 + y2) / 2 - 6, rot, 10, fill='#444', anchor='middle', italic=True)
    s.rect(30, 70, 360, 760, fill=C['moldura'], stroke=C['traco'], r=12, dash=True); s.text(50, 98, 'SITE PÚBLICO (sem login)', 13, 'bold')
    s.rect(420, 70, 230, 760, fill=C['moldura'], stroke=C['traco'], r=12, dash=True); s.text(440, 98, 'ACESSO', 13, 'bold')
    s.rect(680, 70, 690, 760, fill=C['moldura'], stroke=C['traco'], r=12, dash=True); s.text(700, 98, 'ÁREA INTERNA (sessão + PoliticaAcesso)', 13, 'bold')
    def poli(pts, rot='', rx=None, ry=None):
        s.e.append('<polyline points="' + ' '.join(f'{x},{y}' for x, y in pts) + '" fill="none" stroke="#5f5f5f" stroke-width="1.6" marker-end="url(#seta)"/>')
        if rot: s.text(rx, ry, rot, 10, fill='#444', anchor='middle', italic=True)
    ini = no(60, 130, 300, 60, 'index.html', 'Início · vitrine, “Agora na Vibe”', True)
    ag = no(60, 290, 300, 60, 'agendar.html', 'Agendar online (4 passos)')
    conf = no(60, 450, 300, 60, 'Confirmação', '.ics + WhatsApp (mesma página)')
    fid = no(60, 610, 300, 60, 'fidelidade.html', 'Simulador e consulta de pontos')
    seta(ini, ag, '', 'v'); s.text(162, 245, 'Agendar agora / com ele', 10, fill='#444', anchor='start', italic=True)
    seta(ag, conf, '', 'v'); s.text(220, 405, 'Confirmar', 10, fill='#444', anchor='start', italic=True)
    poli([(60, 640), (44, 640), (44, 320), (58, 320)]); s.text(52, 700, '“Agendar com desconto” volta ao agendamento', 10, fill='#444', anchor='start', italic=True)
    poli([(360, 175), (376, 175), (376, 640), (362, 640)]); s.text(372, 560, 'Simular', 10, fill='#444', anchor='end', italic=True)
    lg = no(440, 290, 190, 60, 'login.html', 'Demo: 3 perfis', True)
    rec = no(440, 450, 190, 60, 'recuperar-senha.html', 'Senha provisória')
    poli([(360, 145), (400, 145), (400, 310), (438, 310)]); s.text(404, 136, 'Área da equipe', 10, fill='#444', anchor='middle', italic=True)
    poli([(500, 350), (500, 448)]); s.text(494, 404, 'Esqueci', 10, fill='#444', anchor='end', italic=True)
    poli([(580, 450), (580, 352)]); s.text(586, 404, 'Ir para o login', 10, fill='#444', anchor='start', italic=True)
    dash = no(710, 130, 300, 60, 'dashboard.html', 'KPIs + gráfico + próximos', True, 'DGB')
    poli([(630, 320), (665, 320), (665, 160), (708, 160)]); s.text(670, 245, 'entrar', 10, fill='#444', anchor='start', italic=True)
    s.rect(710, 212, 630, 30, fill=C['escuro'], stroke='none', r=6); s.text(1025, 232, 'MENU LATERAL — itens filtrados pelas permissões do perfil (PoliticaAcesso)', 11, 'bold', '#ffffff', 'middle')
    s.line(860, 190, 860, 212, C['escuro'], 2)
    pags = [('agenda.html', 'lista · linha do tempo · conflito', 'DGB'), ('clientes.html', 'CRUD · histórico · CPF opcional', 'DG'),
            ('atendimento.html', 'recibo ao vivo · pontos', 'DB'), ('funcionarios.html', 'barbeiros e gerentes', 'DG'),
            ('caixa.html', 'movimentos · CSV', 'DGB'), ('servicos.html', 'catálogo e preços', 'D'),
            ('relatorios.html', '6 relatórios · canvas', 'DG'), ('alterar-senha.html', 'força da senha', 'DGB')]
    nos = []
    for i, (t, sub, pf) in enumerate(pags):
        x = 710 + (i % 2) * 330; y = 280 + (i // 2) * 120
        s.line(x + 150, 242, x + 150, y, C['medio'], 1) if i < 2 else None
        nos.append(no(x, y, 300, 60, t, sub, False, pf))
    poli([(860, 340), (860, 398)]); s.text(866, 374, 'Finalizar (?agendamento=id)', 10, fill='#444', anchor='start', italic=True)
    poli([(1010, 145), (1100, 145), (1100, 120), (1180, 120)]) if False else None
    s.text(1180, 160, 'Atalhos do dashboard: + Agendamento, + Cliente, Finalizar', 10, fill='#444', anchor='middle', italic=True)
    s.rect(700, 772, 650, 44, fill='#ffffff', stroke=C['traco'], r=8)
    for i, (p, t) in enumerate([('D', 'Dono'), ('G', 'Gerente'), ('B', 'Barbeiro')]):
        s.circ(724 + i * 110, 794, 10, '#fff'); s.text(724 + i * 110, 798, p, 10, 'bold', anchor='middle'); s.text(740 + i * 110, 798, t, 12)
    s.text(1340, 798, 'Página sem permissão → redireciona ao dashboard', 11, fill='#444', anchor='end', italic=True)
    s.salvar('00_mapa_navegacao')

for f in [mapa, inicio, agendar, fidelidade, login, recuperar, dashboard, agenda, agenda_linha, agenda_modal, clientes, funcionarios, servicos, atendimento, caixa, relatorios, alterar,
          m_inicio, m_agendar, m_login, m_dashboard, m_menu, m_agenda, m_atendimento]:
    f()
print(len(os.listdir(OUT)), 'svg')
