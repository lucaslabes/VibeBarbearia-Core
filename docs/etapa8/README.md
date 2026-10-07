# Etapa 8 — Wireframes, protótipos e testes do front-end web

| Pasta | Conteúdo |
|-------|----------|
| `wireframes/svg/` | 24 wireframes de baixa fidelidade em SVG (editáveis): mapa de navegação, 16 telas desktop e 7 telas de celular |
| `wireframes/png/` | Os mesmos wireframes em PNG |
| `prototipos/` | 34 capturas de alta fidelidade das páginas reais (desktop escuro, tema claro, perfil barbeiro e celular) |
| `Documento_Wireframes_Etapa8.docx` | Documento com mapa de páginas, descrição de cada wireframe, decisões de design e soluções inovadoras |
| `scripts/` | Scripts Python usados para gerar as imagens e testar as páginas |

O projeto web fica em [`../../VibeBarbearia-Web`](../../VibeBarbearia-Web).

## Scripts (opcionais)

Requerem Python 3 e Playwright (`pip install playwright`). Para usar o Chrome instalado, defina `CHROME`
com o caminho do executável; sem a variável, é usado o Chromium do Playwright (`playwright install chromium`).

```bash
# testes de ponta a ponta (70 verificações + erros de console), a partir da raiz do repositório
python3 docs/etapa8/scripts/teste_e2e.py VibeBarbearia-Web/public_html

# regenerar wireframes (SVG → PNG) e protótipos
python3 docs/etapa8/scripts/gerar_wireframes.py docs/etapa8/wireframes/svg
python3 docs/etapa8/scripts/svg_para_png.py docs/etapa8/wireframes/svg docs/etapa8/wireframes/png
python3 docs/etapa8/scripts/gerar_prototipos.py VibeBarbearia-Web/public_html docs/etapa8/prototipos
```

Os testes usam a data do computador: a massa de demonstração é criada a partir do dia atual.
