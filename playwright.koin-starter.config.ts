import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/koin-starter',
  use: { baseURL: 'http://127.0.0.1:4185', ...devices['Desktop Chrome'] },
  webServer: {
    command: 'python3 -m http.server 4185 --bind 127.0.0.1 --directory koin-example/build/dist/js/productionExecutable',
    url: 'http://127.0.0.1:4185',
    reuseExistingServer: !process.env.CI,
  },
});
