import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// dev uses '/', GitHub Pages uses '/delphi-pascal-llvm-compiler/'
// build output goes to ../docs so Pages can serve it
export default defineConfig(({ mode }) => ({
  plugins: [react()],
  base: mode === 'production' ? '/delphi-pascal-llvm-compiler/' : '/',
  build: { outDir: '../docs' },
}))
