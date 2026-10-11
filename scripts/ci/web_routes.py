"""Static entry pages and a Pages redirect for a path-routing demo below a project base."""
from pathlib import Path
import json
import shutil

def assemble_routes(bundle: Path, output: Path, base: str):
    target = output/'routes'
    shutil.copytree(bundle, target, dirs_exist_ok=True)
    # Routed HTML shells add files/change index.html; the canonical bundle owns its manifest.
    (target/'web-assets.json').unlink(missing_ok=True)
    index = (target/'index.html').read_text().replace('<head>', f'<head><base href="{base}">').replace('<body>', f'<body data-route-base="{base}">')
    (target/'index.html').write_text(index)
    for route in ['tasks', 'catalog', 'recipes', 'tasks/welcome', 'tasks/adaptive', 'tasks/customize']:
        page = target/route/'index.html'
        page.parent.mkdir(parents=True, exist_ok=True)
        page.write_text(index)
    # A second application shell uses the same compiled feature bundle.
    shop_base = base.removesuffix("routes/")
    shop_assets = shop_base + "shop/"
    shop = output/'shop'
    shutil.copytree(bundle, shop, dirs_exist_ok=True)
    (shop/'web-assets.json').unlink(missing_ok=True)
    shop_index = (bundle/'index.html').read_text().replace('<head>', f'<head><base href="{shop_assets}">').replace('<body>', f'<body data-route-base="{shop_base}" data-app="shop">')
    (shop/'index.html').write_text(shop_index)
    for route in ['collection', 'cart', 'checkout', 'confirmation', 'products/canvas-weekender', 'products/studio-mug', 'products/clay-table-lamp']:
        page = shop/route/'index.html'
        page.parent.mkdir(parents=True, exist_ok=True)
        page.write_text(shop_index)
    # Unknown task IDs also reload on GitHub Pages. Static URLs above return 200;
    # arbitrary dynamic routes first return 404, then resume the SPA through its entry.
    fallback = '''<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Domposable route</title></head>
<body><p>Opening the Domposable demo…</p><a href="BASE">Open demo</a><script>
const routeBase = BASE_JSON;
const shopBase = SHOP_JSON;
const base = location.pathname.startsWith(shopBase + 'shop/') ? shopBase : routeBase;
if (location.pathname.startsWith(base)) {
  const route = '/' + location.pathname.slice(base.length).replace(/\\/$/, '');
  location.replace((base === shopBase ? shopBase + 'shop/' : base) + '?deepLink=' + encodeURIComponent(route));
}
</script></body></html>'''.replace('BASE_JSON',json.dumps(base)).replace('SHOP_JSON',json.dumps(shop_base)).replace('href="BASE"',f'href="{base}"')
    (output/'404.html').write_text(fallback)
