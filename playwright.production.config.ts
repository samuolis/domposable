import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/production',
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { outputFolder: 'playwright-production-report', open: 'never' }]],
  outputDir: 'production-test-results',
  use: { baseURL: 'http://127.0.0.1:4174', trace: 'retain-on-failure' },
  webServer: {
    command: 'python3 scripts/ci/serve_preview.py --root build/site --prefix /domposable --port 4174',
    url: 'http://127.0.0.1:4174/domposable/',
    reuseExistingServer: !process.env.CI,
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  ],
});
