import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createServer} from 'node:http';
import {mkdtemp, readFile, rm, writeFile} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {chromium} from '@playwright/test';
import {exportSite, exportPages, pageFile} from './index.mjs';

test('exportSite owns lifecycle, waits for content, serves assets and reuses the original shell', async () => {
  const directory = await mkdtemp(join(tmpdir(),'domposable-site-test-'));
  const shell = `<!doctype html><html><head><title>Example</title><meta name="description" content="Example page"><link rel="canonical"></head><body><div id="root" aria-busy="true"></div><script src="/app.js"></script></body></html>`;
  let capturedPage;
  let sourceUrl;
  const options = {directory,canonicalOrigin:'https://example.com',pages:['/about/'],
    settled:async page => { capturedPage = page; sourceUrl = page.url(); }};
  try {
    await writeFile(join(directory,'index.html'),shell);
    await writeFile(join(directory,'data.json'),JSON.stringify({title:'First render'}));
    await writeFile(join(directory,'app.js'),`fetch('/data.json').then(x=>x.json()).then(data=>setTimeout(()=>{
      document.querySelector('link[rel="canonical"]').href='https://example.com'+location.pathname;
      const root=document.querySelector('#root'); root.innerHTML='<h1>'+data.title+'</h1>'; setTimeout(()=>root.removeAttribute('aria-busy'),100);
    },100));`);
    const first = await exportSite({...options,settled:async page=>{
      await options.settled(page);
      assert.equal(await page.locator('#root').getAttribute('aria-busy'),null);
    }});
    assert.equal(first.pages[0].title,'Example');
    assert.match(await readFile(join(directory,'about/index.html'),'utf8'), /<h1>First render<\/h1>/);
    assert.equal(await readFile(join(directory,'app-shell.html'),'utf8'),shell);
    assert.equal(capturedPage.isClosed(),true);
    await assert.rejects(fetch(sourceUrl));
    await writeFile(join(directory,'data.json'),JSON.stringify({title:'Second render'}));
    await exportSite({...options,pages:['/']});
    assert.match(await readFile(join(directory,'index.html'),'utf8'), /<h1>Second render<\/h1>/);
    await assert.rejects(readFile(join(directory,'about/index.html')),{code:'ENOENT'});
    await writeFile(join(directory,'data.json'),JSON.stringify({title:'Third render'}));
    await exportSite({...options,pages:['/']});
    const previous = await readFile(join(directory,'index.html'),'utf8');
    assert.match(previous, /<h1>Third render<\/h1>/);
    await assert.rejects(exportSite({...options,pages:[{path:'/',revision:'updated'}],settled:async page=>{
      capturedPage = page; sourceUrl = page.url(); throw new Error('Host policy rejected');
    }}), /Host policy rejected/);
    assert.equal(await readFile(join(directory,'index.html'),'utf8'),previous);
    assert.equal(capturedPage.isClosed(),true);
    await assert.rejects(fetch(sourceUrl));
    const rebuiltShell = shell.replace('<title>Example</title>', '<title>Rebuilt app</title>');
    await writeFile(join(directory,'index.html'),rebuiltShell);
    const rebuilt = await exportSite({...options,pages:['/']});
    assert.equal(rebuilt.pages[0].title,'Rebuilt app');
    assert.equal(await readFile(join(directory,'app-shell.html'),'utf8'),rebuiltShell);
  } finally { await rm(directory,{recursive:true,force:true}); }
});

test('export paths cannot escape the artifact or include viewer-specific URL state', () => {
  assert.equal(pageFile('/events/42'), join('events','42','index.html'));
  for (const path of ['../secret','//example.com','/a/../secret','/%2e%2e/secret','/a%2fb','/a\\b','/event?share=token','/event#tab','/a%3fb']) {
    assert.throws(() => pageFile(path), undefined, path);
  }
});

test('real browser export keeps CSS/content, rejects private or broken routes, and removes stale owned files', async () => {
  const output = await mkdtemp(join(tmpdir(),'domposable-export-test-'));
  let title = 'Public cup';
  let privatePage = false;
  let broken = false;
  let brokenImage = false;
  const server = createServer((request,response) => {
    if (request.url === '/missing.png') {response.writeHead(404);response.end();return;}
    response.writeHead(broken ? 503 : 200, {'content-type':'text/html', ...(privatePage ? {'x-robots-tag':'noindex'} : {})});
    response.end(`<!doctype html><html><head><title>${title}</title><meta name="description" content="Public event details"><link rel="canonical" href="https://example.com${request.url}"><style id="generated"></style></head><body><div id="root"><h1>${title}</h1><a href="http://${request.headers.host}/about/">About</a><button>Join</button><img src="${brokenImage?'/missing.png':'http://127.0.0.1:1/avatar.png'}" alt="Optional avatar"></div><script>document.getElementById('generated').sheet.insertRule('h1 { color: rgb(1, 2, 3); }');document.cookie='exportLeak=forbidden';</script></body></html>`);
  });
  await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
  const browser = await chromium.launch({headless:true});
  const baseUrl = `http://127.0.0.1:${server.address().port}`;
  const run = pages => exportPages({browser,baseUrl,canonicalOrigin:'https://example.com',outputDir:output,pages,
    prepareContext:async context => assert.deepEqual(await context.cookies(), [])});
  try {
    const manifest = await run([{path:'/events/42',revision:'first'},{path:'/about/'}]);
    assert.equal(manifest.pages.length,2);
    const first = await readFile(join(output,'events/42/index.html'),'utf8');
    assert.match(first,/data-domposable-prerender="true"/);
    assert.match(first,/rgb\(1, 2, 3\)/);
    assert.match(first,/<button inert="">Join/);
    assert.match(first,/<a href="\/about\/">About/);
    assert.match(first,/href="https:\/\/example.com\/events\/42"/);
    const context = await browser.newContext({javaScriptEnabled:false});
    const page = await context.newPage();
    await page.setContent(first);
    assert.equal(await page.locator('h1').textContent(), 'Public cup');
    assert.equal(await page.locator('h1').evaluate(x=>getComputedStyle(x).color), 'rgb(1, 2, 3)');
    await context.close();
    brokenImage = true;
    await assert.rejects(run([{path:'/events/42'}]), /Export failed for \/events\/42/);
    assert.equal(await readFile(join(output,'events/42/index.html'),'utf8'),first);
    brokenImage = false;
    privatePage = true;
    await assert.rejects(run([{path:'/events/42'}]), /public page/);
    assert.equal(await readFile(join(output,'events/42/index.html'),'utf8'),first);
    privatePage = false; broken = true;
    await assert.rejects(run([{path:'/events/42'}]), /public page/);
    broken = false; title = 'Updated public cup';
    const updated = await run([{path:'/events/42',revision:'second'}]);
    assert.notEqual(updated.pages[0].sha256,manifest.pages[0].sha256);
    assert.equal(updated.pages[0].revision,'second');
    await assert.rejects(readFile(join(output,'about/index.html')), {code:'ENOENT'});
    await assert.rejects(run([{path:'/events/42'},{path:'/events/42/'}]), /Duplicate/);
  } finally {
    await browser.close(); server.closeAllConnections();
    await new Promise(resolve=>server.close(resolve));
    await rm(output,{recursive:true,force:true});
  }
});
