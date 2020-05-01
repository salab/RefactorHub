import { Configuration } from '@nuxt/types'
import MonacoEditorWebpackPlugin from 'monaco-editor-webpack-plugin'

export default {
  mode: 'spa',
  head: {
    title: 'RefactorHub',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      {
        hid: 'description',
        name: 'description',
        content: process.env.npm_package_description || '',
      },
    ],
    link: [{ rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }],
  },
  loading: {},
  typescript: { typeCheck: { eslint: true } },
  plugins: ['@/plugins/composition-api', '@/plugins/axios', '@/plugins/client'],
  build: {
    transpile: [/typed-vuex/],
    extend(config) {
      if (config.plugins) config.plugins.push(new MonacoEditorWebpackPlugin())
    },
  },
  buildModules: [
    '@nuxt/typescript-build',
    '@nuxtjs/eslint-module',
    '@nuxtjs/vuetify',
    'nuxt-typed-vuex',
  ],
  modules: ['@nuxtjs/axios', '@nuxtjs/auth', '@nuxtjs/proxy'],
  vuetify: {
    customVariables: ['@/assets/styles/variables.scss'],
    defaultAssets: {
      icons: 'fa',
    },
    treeShake: true,
  },
  axios: {
    proxy: true,
  },
  proxy: {
    '/api': process.env.API_URL || 'http://localhost:8080',
  },
  auth: {
    redirect: {
      login: '/login',
      logout: '/',
      callback: '/callback',
    },
    strategies: {
      github: {
        client_id: process.env.CLIENT_ID,
        client_secret: process.env.CLIENT_SECRET,
        userinfo_endpoint: '/api/user/me',
        scope: ['read:user'],
      },
    },
  },
} as Configuration
