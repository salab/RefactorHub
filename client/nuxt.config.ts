import { Configuration } from '@nuxt/types'
import MonacoEditorWebpackPlugin from 'monaco-editor-webpack-plugin'

const config: Configuration = {
  mode: 'spa',
  /*
   ** Headers of the page
   */
  head: {
    title: 'RefactorHub',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      {
        hid: 'description',
        name: 'description',
        content: process.env.npm_package_description || ''
      }
    ],
    link: [{ rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }]
  },
  /*
   ** Customize the progress-bar color
   */
  loading: {},
  /*
   ** Global CSS
   */
  css: [],
  /*
   ** Plugins to load before mounting the App
   */
  plugins: ['~/plugins/axios', '~/plugins/client', '~/plugins/editor'],
  /*
   ** Nuxt.js dev-modules
   */
  buildModules: [
    '@nuxt/typescript-build',
    // Doc: https://github.com/nuxt-community/eslint-module
    '@nuxtjs/eslint-module',
    '@nuxtjs/vuetify',
    'nuxt-typed-vuex'
  ],
  /*
   ** Nuxt.js modules
   */
  modules: [
    // Doc: https://axios.nuxtjs.org/usage
    '@nuxtjs/axios',
    '@nuxtjs/auth',
    '@nuxtjs/proxy',
    '@nuxtjs/eslint-module'
  ],
  /*
   ** Axios module configuration
   ** See https://axios.nuxtjs.org/options
   */
  axios: {
    proxy: true
  },
  /*
   ** vuetify module configuration
   ** https://github.com/nuxt-community/vuetify-module
   */
  vuetify: {
    customVariables: ['~/assets/styles/variables.scss'],
    defaultAssets: {
      icons: 'fa'
    },
    theme: {},
    treeShake: true
  },
  /*
   ** Build configuration
   */
  build: {
    transpile: [/nuxt-typed-vuex/],
    /*
     ** You can extend webpack config here
     */
    extend(config) {
      if (config.plugins) config.plugins.push(new MonacoEditorWebpackPlugin())
    }
  },
  proxy: {
    '/api': process.env.API_URL || 'http://localhost:8080'
  },
  auth: {
    redirect: {
      login: '/login',
      logout: '/',
      callback: '/callback'
    },
    strategies: {
      github: {
        client_id: process.env.CLIENT_ID,
        client_secret: process.env.CLIENT_SECRET,
        userinfo_endpoint: '/api/user/me',
        scope: ['read:user']
      }
    }
  }
}

export default config
