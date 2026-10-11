"""Static entry pages and a Pages redirect for a path-routing demo below a project base."""
from pathlib import Path
import json
import shutil

def assemble_routes(bundle: Path, output: Path, base: str):
    target = output/'routes'
    shutil.copytree(bundle, target, dirs_exist_ok=True)
    index = (target/'index.html').read_text().replace('<head>', f'<head><base href="{base}">').replace('<body>', f'<body data-route-base="{base}">')
    (target/'index.html').write_text(index)
    for route in ['tasks', 'catalog', 'recipes', 'tasks/welcome', 'tasks/adaptive', 'tasks/customize']:
        page = target/route/'index.html'
        page.parent.mkdir(parents=True, exist_ok=True)
        page.write_text(index)
    # Unknown task IDs also reload on GitHub Pages. Static URLs above return 200;
    # arbitrary dynamic routes first return 404, then resume the SPA through its entry.
    fallback = '''<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Domposable route</title></head>
<body><p>Opening the Domposable demo…</p><a href="BASE">Open demo</a><script>
const base = BASE_JSON;
if (location.pathname.startsWith(base)) {
  const route = '/' + location.pathname.slice(base.length).replace(/\\/$/, '');
  location.replace(base + '?deepLink=' + encodeURIComponent(route));
}
</script></body></html>'''.replace('BASE_JSON',json.dumps(base)).replace('href="BASE"',f'href="{base}"')
    (output/'404.html').write_text(fallback)
