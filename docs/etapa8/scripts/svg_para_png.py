import sys, os, re
from playwright.sync_api import sync_playwright
src, dst = os.path.abspath(sys.argv[1]), os.path.abspath(sys.argv[2]); os.makedirs(dst, exist_ok=True)
with sync_playwright() as p:
    b = p.chromium.launch(**({'executable_path': os.environ['CHROME']} if os.environ.get('CHROME') else {}))
    for f in sorted(os.listdir(src)):
        svg = open(os.path.join(src, f), encoding='utf-8').read()
        w, h = map(int, re.search(r'width="(\d+)" height="(\d+)"', svg).groups())
        pg = b.new_page(viewport={'width': w, 'height': h}, device_scale_factor=1.5 if w < 500 else 1.25)
        pg.goto('file://' + os.path.join(src, f)); pg.wait_for_timeout(100)
        pg.screenshot(path=os.path.join(dst, f.replace('.svg', '.png'))); pg.close()
    b.close()
print(len(os.listdir(dst)))
