import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/v1/telemetry': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/v1/predict': {
        target: 'http://localhost:8000',
        changeOrigin: true
      },
      '/api/v1/jobs': {
        target: 'http://localhost:8000',
        changeOrigin: true
      },
      '/api/v1/models': {
        target: 'http://localhost:8000',
        changeOrigin: true
      },
      '/api/v1': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts'
  }
})
