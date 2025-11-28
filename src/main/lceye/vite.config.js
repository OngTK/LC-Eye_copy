import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    // react-query 사용 시 중복 React 로딩을 방지하기 위해 dedupe 설정
    dedupe: ['react', 'react-dom'],
  },
})
