#!/usr/bin/env python3
"""Local static server reproducing GitHub Pages' HTML 404 fallback for route tests."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit
import argparse

ROOT = Path(__file__).resolve().parents[2]/'build/browser-preview'
PREFIX = ''
class PagesHandler(SimpleHTTPRequestHandler):
    def translate_path(self, path):
        if PREFIX:
            requested = urlsplit(path).path
            if requested == PREFIX: path = '/'
            elif requested.startswith(PREFIX + '/'): path = path[len(PREFIX):]
            else: return str(ROOT / '__outside_project_base__')
        return super().translate_path(path)
    def send_error(self, code, message=None, explain=None):
        fallback = ROOT/'404.html'
        if code == 404 and urlsplit(self.path).path.startswith((PREFIX + '/routes/', PREFIX + '/shop/')) and fallback.is_file():
            body = fallback.read_bytes()
            self.send_response(404)
            self.send_header('Content-Type','text/html; charset=utf-8')
            self.send_header('Content-Length',str(len(body)))
            self.end_headers()
            if self.command != 'HEAD': self.wfile.write(body)
        else: super().send_error(code,message,explain)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', type=Path, default=ROOT)
    parser.add_argument('--prefix', default='')
    parser.add_argument('--port', type=int, default=4173)
    args = parser.parse_args()
    ROOT = args.root.resolve()
    PREFIX = args.prefix.rstrip('/')
    with ThreadingHTTPServer(('127.0.0.1',args.port), partial(PagesHandler,directory=str(ROOT))) as server:
        server.serve_forever()
