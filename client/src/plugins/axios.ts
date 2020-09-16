import { defineNuxtPlugin } from '@nuxtjs/composition-api'

export default defineNuxtPlugin(({ $axios }) => {
  $axios.defaults.withCredentials = true
  $axios.defaults.xsrfCookieName = 'XSRF-TOKEN'
  $axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN'
})
