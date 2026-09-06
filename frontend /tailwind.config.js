/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        harbor: '#0F2A3D',
        coral: '#EB6649', // 로고의 메인 컬러로 맞춤
        sunbeam: '#F4B942',
        seaglass: '#4E8B87',
        sand: '#F6EFE4',
        mist: '#6B7C85',
      },
      fontFamily: {
        sans: ['Pretendard', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace'],
      },
    },
  },
  plugins: [],
}
