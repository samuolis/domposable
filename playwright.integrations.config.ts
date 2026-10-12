import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/integrations',
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'integration-playwright-report' }]],
  outputDir: 'integration-test-results',
  use: { baseURL: 'http://127.0.0.1:4183', trace: 'retain-on-failure' },
  webServer: {
    command: 'python3 scripts/ci/serve_kobweb.py',
    url: 'http://127.0.0.1:4183', reuseExistingServer: !process.env.CI,
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  ],
});
