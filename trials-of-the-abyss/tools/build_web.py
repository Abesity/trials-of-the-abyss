"""Copies the game's art, sound and font from res/ into web/assets/ for the browser version.
Tiles are packed into one atlas (web/assets/tiles.png, 20 tiles per row) to save requests.
Needs: pip install pillow fonttools brotli

    python tools/build_web.py
"""
import os, shutil
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES, OUT = os.path.join(ROOT, 'res'), os.path.join(ROOT, 'web', 'assets')
shutil.rmtree(OUT, ignore_errors=True)
for d in ('player', 'battle', 'monster', 'objects', 'spikes', 'sound', 'fonts'):
    shutil.copytree(os.path.join(RES, d), os.path.join(OUT, d))
atlas = Image.new('RGBA', (20 * 16, 10 * 16), (0, 0, 0, 0))
for i in range(197):
    t = Image.open(os.path.join(RES, 'tiles', f'{i:02d}.png')).convert('RGBA')
    atlas.paste(t, ((i % 20) * 16, (i // 20) * 16))
atlas.save(os.path.join(OUT, 'tiles.png'))
# subset the pixel font to printable ASCII for the web (the full CJK font is ~3 MB; its license allows modification)
from fontTools import subset
font_dir = os.path.join(OUT, 'fonts')
opts = subset.Options(); opts.flavor = 'woff2'
f = subset.load_font(os.path.join(font_dir, 'x12y16pxMaruMonica.ttf'), opts)
sub = subset.Subsetter(opts); sub.populate(unicodes=range(0x20, 0x7f)); sub.subset(f)
subset.save_font(f, os.path.join(font_dir, 'maru-monica-latin.woff2'), opts)
os.remove(os.path.join(font_dir, 'x12y16pxMaruMonica.ttf'))
print('web/assets ready:', sum(len(f) for _, _, f in os.walk(OUT)), 'files')
