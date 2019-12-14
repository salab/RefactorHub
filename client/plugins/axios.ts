import { Plugin } from '@nuxt/types'

const axios: Plugin = ({ $axios }) => {
  $axios.defaults.withCredentials = true
  $axios.defaults.xsrfCookieName = 'XSRF-TOKEN'
  $axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN'
}

export default axios
