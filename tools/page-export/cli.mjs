import {readFile} from 'node:fs/promises';
import {exportSite} from './index.mjs';

const manifest = await exportSite(JSON.parse(await readFile(process.argv[2], 'utf8')));
console.log(`Exported ${manifest.pages.length} public pages`);
