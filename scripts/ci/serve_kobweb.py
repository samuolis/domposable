"""Serve Kobweb's static export with extensionless HTML URLs, without an SPA fallback."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2] / "kobweb-example/.kobweb/site"


class ExportHandler(SimpleHTTPRequestHandler):
    def translate_path(self, path):
        result = super().translate_path(path)
        # A dynamic route creates a directory beside its parent route's HTML.
        # Prefer the exported page over a directory listing or slash redirect.
        page = Path(str(Path(result)) + ".html")
        if page.is_file():
            return str(page)
        return result


if __name__ == "__main__":
    with ThreadingHTTPServer(("127.0.0.1", 4183), partial(ExportHandler, directory=str(ROOT))) as server:
        server.serve_forever()
