// Existing application rendering; export configuration lives entirely in build.gradle.kts.
document.querySelector('link[rel="canonical"]').href = 'https://example.com' + location.pathname;
document.querySelector('#generated').sheet.insertRule('h1 { color: rgb(1, 2, 3); }');
const title = location.pathname === '/about/' ? 'About' : 'Event 42';
const root = document.querySelector('#root');
root.innerHTML = `<h1>${title}</h1><a href="/about/">About</a><button>Join</button>`;
root.removeAttribute('aria-busy');
