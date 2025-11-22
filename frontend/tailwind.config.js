/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#ff4f00', // Zomato Red
        secondary: '#2d3748',
      }
    },
  },
  plugins: [],
}
