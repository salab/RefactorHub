import vuetify from 'vite-plugin-vuetify'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'

export default defineNuxtConfig({
  srcDir: 'src',
  app: {
    head: {
      link: [
        {
          rel: 'preconnect',
          href: 'https://fonts.googleapis.com',
        },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css?family=Roboto:100,200,300,400,500,700,900&display=swap',
          crossorigin: '',
        },
      ],
    },
  },
  ssr: false,
  components: [
    {
      path: '~/components/',
      extensions: ['.vue'],
      pathPrefix: false,
    },
  ],
  build: {
    transpile: ['vuetify'],
  },
  modules: [],
  hooks: {
    'vite:extendConfig': (config) => {
      config.plugins?.push(vuetify())
    },
  },
  typescript: {
    strict: true,
    typeCheck: true,
  },
  vite: {
    ssr: {
      noExternal: ['vuetify'],
    },
    define: {
      'process.env.DEBUG': false,
    },
    plugins: [monacoEditorPlugin({})],
    server: {
      proxy: {
        '/api': process.env.API_URL || 'http://localhost:8080',
        '/login': 'http://localhost:8080',
      },
    },
  },
  css: ['@/assets/main.scss'],
  devtools: { enabled: true },
})
