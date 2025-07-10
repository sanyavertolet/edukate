import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 80,
    proxy: {
      '/api': {
        target: 'http://localhost:5810',
        changeOrigin: true,
      },
      '/auth': {
        target: 'http://localhost:5810',
        changeOrigin: true,
      }
    },
  },
  optimizeDeps: {
    exclude: ['js-big-decimal']
  }
})
