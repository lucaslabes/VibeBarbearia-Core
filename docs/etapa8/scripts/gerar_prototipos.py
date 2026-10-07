import sys, os
from playwright.sync_api import sync_playwright
BASE = 'file://' + os.path.abspath(sys.argv[1]) + '/'; OUT = sys.argv[2]; os.makedirs(OUT, exist_ok=True)
for f in os.listdir(OUT):
    if f.endswith('.png'): os.remove(os.path.join(OUT, f))
erros = []
def ctx_novo(b, w, h, tema='escuro', dsf=1):
    c = b.new_context(viewport={'width': w, 'height': h}, device_scale_factor=dsf); pg = c.new_page()
    pg.on('pageerror', lambda e: erros.append(str(e))); pg.on('console', lambda m: m.type == 'error' and erros.append(m.text))
    pg.goto(BASE + 'index.html'); pg.evaluate(f"localStorage.setItem('vibe.tema','{tema}')"); return c, pg
def shot(pg, nome, full=False):
    pg.wait_for_timeout(450); pg.screenshot(path=os.path.join(OUT, nome + '.png'), full_page=full); print(nome)
def entrar(pg, u='admin', s='admin'):
    pg.goto(BASE + 'login.html'); pg.fill('#login-usuario', u); pg.fill('#login-senha', s); pg.click('#form-login button[type=submit]'); pg.wait_for_url('**/dashboard.html')
def agendar_ate_passo3(pg):
    pg.goto(BASE + 'agendar.html'); pg.wait_for_timeout(300)
    pg.click('label:has(input[name="servicos"]) >> nth=2'); pg.click('label:has(input[name="servicos"]) >> nth=0'); pg.click('#btn-avancar')
    pg.click('label:has(input[name="barbeiro"]) >> nth=1'); pg.click('#btn-avancar'); pg.wait_for_timeout(200)
    pg.click('#op-horas .horario:not([disabled]) >> nth=2')
with sync_playwright() as p:
    b = p.chromium.launch(**({'executable_path': os.environ['CHROME']} if os.environ.get('CHROME') else {}))
    # ---------- desktop escuro ----------
    c, pg = ctx_novo(b, 1366, 860)
    pg.goto(BASE + 'index.html'); shot(pg, '01_inicio', full=True)
    agendar_ate_passo3(pg); shot(pg, '02_agendar_horarios')
    pg.click('#btn-avancar'); pg.fill('#ag-nome', 'Mateus Ribeiro'); pg.fill('#ag-telefone', '51991234567'); shot(pg, '03_agendar_dados')
    pg.click('#btn-confirmar'); shot(pg, '04_agendar_confirmado')
    pg.goto(BASE + 'fidelidade.html'); shot(pg, '05_fidelidade', full=True)
    pg.goto(BASE + 'login.html'); shot(pg, '06_login')
    pg.fill('#login-usuario', 'admin'); pg.fill('#login-senha', 'x'); pg.click('#form-login button[type=submit]'); shot(pg, '07_login_erro')
    pg.goto(BASE + 'recuperar-senha.html'); pg.fill('#rec-login', '555.555.555-55'); pg.click('#form-recuperar button[type=submit]'); shot(pg, '08_recuperar_senha')
    entrar(pg); shot(pg, '09_dashboard')
    pg.goto(BASE + 'agenda.html'); shot(pg, '10_agenda_lista')
    pg.click('button:has-text("Linha do tempo")'); pg.evaluate("document.querySelector('#linha-tempo').scrollIntoView(); window.scrollBy(0, 300)"); shot(pg, '11_agenda_linha_tempo')
    pg.click('#btn-novo'); pg.wait_for_timeout(200)
    pg.select_option('#ag-cliente', '9'); pg.select_option('#ag-barbeiro', '1')
    hoje = pg.evaluate("Vibe.Regras.formatarData(Vibe.UI.hojeIso())"); pg.fill('#ag-data', hoje); pg.fill('#ag-hora', '14:15')
    pg.click('#form-agendamento button[type=submit]'); shot(pg, '12_agenda_conflito')
    pg.keyboard.press('Escape')
    pg.goto(BASE + 'clientes.html'); shot(pg, '13_clientes')
    pg.click('#btn-novo'); pg.fill('#cli-nome', 'Ana Paula'); pg.fill('#cli-telefone', '51988887777'); pg.fill('#cli-cpf', '12345678900'); pg.fill('#cli-email', 'ana@email.com')
    pg.click('#form-cliente button[type=submit]'); shot(pg, '14_clientes_cpf_invalido')
    pg.keyboard.press('Escape')
    pg.goto(BASE + 'funcionarios.html'); shot(pg, '15_funcionarios')
    pg.goto(BASE + 'servicos.html'); shot(pg, '16_servicos')
    ag = pg.evaluate("Vibe.Dados.listar('agendamentos').find(a=>a.status==='AGENDADO'&&a.dataHora.endsWith('14:30')&&a.dataHora.startsWith(Vibe.UI.hojeIso())).id")
    pg.goto(BASE + f'atendimento.html?agendamento={ag}'); pg.wait_for_timeout(300)
    for i in pg.evaluate("Vibe.Dados.listar('servicos').filter(s=>['Corte + Barba','Platinado / Descoloração'].includes(s.nome)).map(s=>s.id)"):
        pg.check(f'#lista-itens input[value="{i}"]')
    pg.click('label.chip:has(input[value="PIX"])'); pg.check('#at-resgatar'); pg.evaluate('window.scrollTo(0,0)'); shot(pg, '17_atendimento_recibo')
    pg.click('#form-atendimento button[type=submit]'); shot(pg, '18_atendimento_concluido')
    pg.goto(BASE + 'caixa.html'); shot(pg, '19_caixa')
    pg.goto(BASE + 'relatorios.html'); shot(pg, '20_relatorios')
    pg.click('#aba-barbeiros'); shot(pg, '21_relatorios_barbeiros')
    pg.goto(BASE + 'alterar-senha.html'); pg.fill('#s-atual', 'admin'); pg.fill('#s-nova', 'Vibe2026'); shot(pg, '22_alterar_senha')
    c.close()
    # ---------- tema claro ----------
    c, pg = ctx_novo(b, 1366, 860, 'claro')
    pg.goto(BASE + 'index.html'); shot(pg, '23_inicio_claro')
    entrar(pg); shot(pg, '24_dashboard_claro')
    pg.goto(BASE + 'agenda.html'); pg.click('button:has-text("Linha do tempo")'); pg.evaluate("window.scrollBy(0, 360)"); shot(pg, '25_agenda_claro')
    c.close()
    # ---------- perfil barbeiro ----------
    c, pg = ctx_novo(b, 1366, 860)
    entrar(pg, '111.111.111-11', '123'); shot(pg, '26_dashboard_barbeiro')
    c.close()
    # ---------- mobile ----------
    c, pg = ctx_novo(b, 390, 844, dsf=2)
    pg.goto(BASE + 'index.html'); shot(pg, '27_mobile_inicio')
    pg.click('#btn-menu-site'); shot(pg, '28_mobile_inicio_menu')
    agendar_ate_passo3(pg); pg.evaluate("document.querySelector('#op-horas').scrollIntoView({block:'center'})"); shot(pg, '29_mobile_agendar')
    pg.goto(BASE + 'login.html'); shot(pg, '30_mobile_login')
    entrar(pg); shot(pg, '31_mobile_dashboard')
    pg.click('#btn-menu'); shot(pg, '32_mobile_menu')
    pg.goto(BASE + 'agenda.html'); pg.evaluate("document.querySelector('#tabela-agenda').scrollIntoView()"); shot(pg, '33_mobile_agenda')
    pg.goto(BASE + 'atendimento.html'); shot(pg, '34_mobile_atendimento')
    c.close(); b.close()
print('erros:', erros)
