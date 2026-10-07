# Vibe Barbearia — Web (front-end) · Etapa 8

Front-end web do sistema **Vibe Barbearia** em **HTML5 + CSS3 + JavaScript puro** (sem frameworks,
sem build, sem back-end). As telas reproduzem o sistema desktop (Swing) e as validações espelham as
regras de negócio do projeto **VibeBarbearia-Core** (Etapa 6). Os dados ficam no `localStorage` do
navegador, então todo o CRUD funciona sem servidor.

## Como abrir

**Direto no navegador:** abra `public_html/index.html` com dois cliques (funciona em `file://`;
não há módulos ES nem requisições de rede).

**NetBeans (HTML5/JavaScript Application):**
1. *File → Open Project* → selecione a pasta `VibeBarbearia-Web` (ícone de globo).
2. *Run Project* (F6) abre `index.html` no navegador configurado (Chrome por padrão; pode ser
   trocado em *Project Properties → Run*). O servidor interno do NetBeans é usado (`server=INTERNAL`).

## Acessos de demonstração

| Perfil   | Usuário            | Senha   | O que vê |
|----------|--------------------|---------|----------|
| Dono     | `admin`            | `admin` | Todas as telas |
| Gerente  | `555.555.555-55`   | `123`   | Dashboard, Agenda, Clientes, Funcionários (consulta), Caixa, Relatórios |
| Barbeiro | `111.111.111-11` (até `444.444.444-44`) | `123` | Dashboard próprio, Agenda (consulta), Atendimento, Caixa próprio |

Na tela de login há botões que preenchem esses acessos e o botão **Restaurar dados de demonstração**
(apaga o `localStorage` e recria a massa de dados).

> As senhas de demonstração ficam em texto puro no `localStorage` apenas porque não existe back-end.
> No sistema real a autenticação é feita pelo Core (`AutenticacaoService` + `CodificadorSenha`).

## Páginas

| Página | Função |
|--------|--------|
| `index.html` | Site público: vitrine, “Agora na Vibe” (próximo horário livre calculado em tempo real), serviços, equipe |
| `agendar.html` | **Agendamento online** em 4 passos com horários gerados pela regra de conflito de 30 min; confirmação com arquivo `.ics` e link do WhatsApp |
| `fidelidade.html` | **Simulador de pontos** (R$ 10 = 1 ponto; 100 pontos = R$ 10; desconto até 50%) e consulta de saldo pelo celular |
| `login.html` | Login com perfis e acessos de demonstração |
| `recuperar-senha.html` | Gera senha provisória (simula o envio por e-mail) |
| `dashboard.html` | KPIs do dia/mês/ano, gráfico de faturamento (canvas) e próximos atendimentos |
| `agenda.html` | Agenda em lista e **linha do tempo**; bloqueio de conflito com sugestão de horários livres |
| `clientes.html` | CRUD de clientes, histórico e pontos |
| `funcionarios.html` | Barbeiros e gerentes, com as regras de permissão do Core |
| `servicos.html` | Catálogo de serviços e produtos (somente Dono) |
| `atendimento.html` | Finalização com **recibo ao vivo**: desconto, comissão e pontos |
| `caixa.html` | Movimentos com filtros, totais e exportação CSV |
| `relatorios.html` | 6 relatórios com gráficos em canvas, CSV e impressão |
| `alterar-senha.html` | Troca de senha com medidor de força |

## Estrutura

```
VibeBarbearia-Web/
├── nbproject/            Projeto NetBeans HTML5 (org.netbeans.modules.web.clientproject)
└── public_html/
    ├── *.html            14 páginas
    ├── css/              base.css (cores do EstiloVibe, temas), componentes.css, layout.css, paginas.css
    ├── js/
    │   ├── tema.js       Aplica tema escuro/claro antes da renderização
    │   ├── regras.js     Regras puras (espelho do Core): validações, comissão, fidelidade, conflito
    │   ├── dados-mock.js Repositório em localStorage + massa de dados de demonstração
    │   ├── auth.js       Sessão e PoliticaAcesso (perfis e permissões)
    │   ├── servicos.js   Casos de uso (Clientes, Funcionários, Agenda, Atendimento, Caixa...)
    │   ├── ui.js         Componentes de interface (toasts, modais, máscaras, erros inline)
    │   ├── graficos.js   Gráficos em <canvas> (barras, linha, rosca)
    │   └── paginas/      Um script por página
    └── img/              logo.svg, favicon.svg
```

As camadas seguem a mesma separação do Core: **regras → dados → serviços → interface**.

## Regras do Core reproduzidas

- Comissão: `round(valor × % ) / 100`, percentual de 0 a 100 (padrão 40%); só o Dono altera.
- Fidelidade: R$ 10 = 1 ponto (sobre o valor pago); 100 pontos = R$ 10 de desconto, até 50% do atendimento.
- Conflito: mesmo barbeiro, agendamento pendente, intervalo menor que 30 minutos.
- Telefone com 10 ou 11 dígitos; e-mail com `@` no meio; datas `dd/MM/aaaa` estritas (31/02 é rejeitado).
- Senha: mínimo 3 caracteres, confirmação igual e diferente da atual; senha provisória de 8 caracteres.
- Perfis Dono / Gerente / Barbeiro com a mesma matriz de permissões da `PoliticaAcesso`.

**Diferenças em relação ao Core (somente na versão web):**
- CPF opcional no cadastro de **clientes**, validado com dígitos verificadores (o `Cliente` do Core não tem CPF).
  Para funcionários, os dígitos verificadores são exigidos apenas em CPF novo ou alterado, porque os
  logins de demonstração (111.111.111-11 etc.) vêm do banco original.
- Horário de funcionamento do agendamento online (segunda a sábado, 09:00–18:30, até 14 dias à frente).

## Acessibilidade e responsividade

HTML semântico (`header`, `nav`, `main`, `table` com `scope`), `label` em todos os campos, erros
inline ligados por `aria-describedby` + `aria-invalid`, link “Pular para o conteúdo”, foco visível,
`prefers-reduced-motion`, gráficos com `role="img"` e descrição textual. Layout responsivo com
menu lateral recolhível (telas de até 1024 px) e tabelas que viram cartões no celular (até 700 px).
Tema escuro (padrão, cores do desktop) e tema claro com contraste ajustado.
