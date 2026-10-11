#!/usr/bin/env python3
"""Local static server reproducing GitHub Pages' HTML 404 fallback for route tests."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[2]/'build/browser-preview'
class PagesHandler(SimpleHTTPRequestHandler):
    def send_error(self, code, message=None, explain=None):
        fallback = ROOT/'404.html'
        if code == 404 and urlsplit(self.path).path.startswith(('/routes/', '/shop/')) and fallback.is_file():
            body = fallback.read_bytes()
            self.send_response(404)
            self.send_header('Content-Type','text/html; charset=utf-8')
            self.send_header('Content-Length',str(len(body)))
            self.end_headers()
            if self.command != 'HEAD': self.wfile.write(body)
        else: super().send_error(code,message,explain)

if __name__ == '__main__':
    with ThreadingHTTPServer(('127.0.0.1',4173), partial(PagesHandler,directory=str(ROOT))) as server:
        server.serve_forever()
