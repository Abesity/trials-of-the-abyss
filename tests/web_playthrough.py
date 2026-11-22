# Plays the browser version to the ending with Playwright, blocking all outside network requests.
# Serve web/ first (python -m http.server 8866 -d web), then: python tests/web_playthrough.py
import asyncio, base64, json, sys, os
from playwright.async_api import async_playwright
URL=sys.argv[1] if len(sys.argv)>1 else 'http://127.0.0.1:8866/'; OUT=sys.argv[2] if len(sys.argv)>2 else 'tests/out/web'; os.makedirs(OUT,exist_ok=True)
async def main():
    async with async_playwright() as pw:
        b=await pw.chromium.launch(); ctx=await b.new_context(viewport={'width':1280,'height':900})
        blocked=[]
        async def route(r):
            if r.request.url.startswith(URL): await r.continue_()
            else: blocked.append(r.request.url); await r.abort()
        await ctx.route('**/*',route)
        pg=await ctx.new_page(); errors=[]
        pg.on('pageerror',lambda e: errors.append(str(e))); pg.on('console',lambda m: errors.append(m.text) if m.type=='error' else None)
        await pg.goto(URL); await pg.wait_for_function('window.__toa && !document.getElementById("loading")',timeout=20000)
        await pg.screenshot(path=f'{OUT}/page_title.png')
        bot=open(os.path.join(os.path.dirname(__file__),'web-playthrough.js')).read()
        res=await pg.evaluate(bot,{'shots':True})
        for l in res['log']: print(l)
        for i,(n,d) in enumerate(res['shots']): open(f'{OUT}/{i:02d}_{n}.png','wb').write(base64.b64decode(d.split(',')[1]))
        print('RESULT', 'PASS' if res['ok'] else 'FAIL', '| page errors:', errors, '| blocked external:', blocked)
        await b.close()
asyncio.run(main())
